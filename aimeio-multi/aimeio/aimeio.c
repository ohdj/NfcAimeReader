#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <openssl/sha.h>
#include <openssl/bio.h>
#include <openssl/evp.h>

#define AIME_ID_SIZE 10
#define MAX_AIME_CARDS 16
#define CFG_NAME L".\\segatools.ini"
#define WEBSOCKET_PORT 8080
#define WEBSOCKET_BUFFER_SIZE 1024

#include "aimeio/aimeio.h"

// Websocket frame opcodes
#define WS_OPCODE_CONTINUATION 0x0
#define WS_OPCODE_TEXT 0x1
#define WS_OPCODE_BINARY 0x2
#define WS_OPCODE_CLOSE 0x8
#define WS_OPCODE_PING 0x9
#define WS_OPCODE_PONG 0xA

enum WebSocketConnectionStatus {
    WS_DISCONNECTED = 0,
    WS_CONNECTING = 1,
    WS_CONNECTED = 2
};

struct aimeio_multi_ctx
{
    uint8_t aime_ids[MAX_AIME_CARDS][AIME_ID_SIZE];
    uint8_t aime_vk[MAX_AIME_CARDS];
    size_t aime_count;
    ssize_t current_aime;
    SOCKET websocket_server;
    SOCKET websocket_client;
    enum WebSocketConnectionStatus connection_status;
    char last_received_card_id[AIME_ID_SIZE * 2 + 1]; // Hex string representation
};

static struct aimeio_multi_ctx ctx = {
    .current_aime = -1,
    .connection_status = WS_DISCONNECTED,
    .last_received_card_id = {0}
};
static HANDLE websocket_thread;
static CRITICAL_SECTION connection_status_mutex;
static volatile bool websocket_running = false;

int base64_encode(const unsigned char* input, int length, char* output, int output_size) {
    BIO *bmem, *b64;
    int encoded_length;

    // Create base64 bio filter
    b64 = BIO_new(BIO_f_base64());
    bmem = BIO_new(BIO_s_mem());
    b64 = BIO_push(b64, bmem);

    // Disable newline for base64 encoding
    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);

    // Write input to base64 filter
    BIO_write(b64, input, length);
    BIO_flush(b64);

    // Get encoded data length
    encoded_length = BIO_get_mem_data(bmem, NULL);

    // Check if output buffer is large enough
    if (encoded_length > output_size - 1) {
        BIO_free_all(b64);
        return -1;  // Buffer too small
    }

    // Read encoded data
    encoded_length = BIO_read(bmem, output, encoded_length);
    output[encoded_length] = '\0';  // Null-terminate

    // Free BIO resources
    BIO_free_all(b64);

    return encoded_length;
}

// WebSocket frame decoding
int decode_websocket_frame(const char* buffer, int buffer_len, char* payload, int payload_size) {
    if (buffer_len < 2) return -1;

    uint8_t opcode = buffer[0] & 0x0F;
    uint8_t mask_bit = (buffer[1] & 0x80);
    uint64_t payload_length = buffer[1] & 0x7F;
    int header_size = 2;

    // Handle extended payload lengths
    if (payload_length == 126) {
        if (buffer_len < 4) return -1;
        payload_length = (buffer[2] << 8) | buffer[3];
        header_size = 4;
    } else if (payload_length == 127) {
        if (buffer_len < 10) return -1;
        return -1; // 64-bit payload length not handled
    }

    // Check mask
    const char* mask_key = mask_bit ? buffer + header_size : NULL;
    header_size += mask_bit ? 4 : 0;

    // Validate payload length
    if (header_size + payload_length > buffer_len) return -1;

    // Handle close frame specifically
    if (opcode == WS_OPCODE_CLOSE) return 0;

    // Unmask payload if needed
    if (mask_key) {
        for (uint64_t i = 0; i < payload_length && i < (uint64_t)(payload_size - 1); i++) {
            payload[i] = buffer[header_size + i] ^ mask_key[i % 4];
        }
        payload[payload_length] = '\0';
    } else {
        strncpy(payload, buffer + header_size, payload_size);
        payload[payload_length] = '\0';
    }

    return payload_length;
}

// WebSocket frame encoding
int encode_websocket_frame(char* buffer, int buffer_size, const char* payload, int payload_len, uint8_t opcode) {
    if (buffer_size < payload_len + 10) return -1;

    // First byte: FIN bit set (0x80) and opcode
    buffer[0] = 0x80 | (opcode & 0x0F);

    // Payload length
    if (payload_len < 126) {
        buffer[1] = payload_len;
        memcpy(buffer + 2, payload, payload_len);
        return payload_len + 2;
    } else if (payload_len < 65536) {
        buffer[1] = 126;
        buffer[2] = (payload_len >> 8) & 0xFF;
        buffer[3] = payload_len & 0xFF;
        memcpy(buffer + 4, payload, payload_len);
        return payload_len + 4;
    }

    return -1;
}

// Status update
void update_connection_status(enum WebSocketConnectionStatus status) {
    EnterCriticalSection(&connection_status_mutex);
    ctx.connection_status = status;
    LeaveCriticalSection(&connection_status_mutex);
}

enum WebSocketConnectionStatus get_connection_status() {
    enum WebSocketConnectionStatus status;
    EnterCriticalSection(&connection_status_mutex);
    status = ctx.connection_status;
    LeaveCriticalSection(&connection_status_mutex);
    return status;
}

// WebSocket handshake
HRESULT handle_websocket_handshake(SOCKET client_socket, const char* websocket_key) {
    // WebSocket magic string
    const char* magic_string = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    char combined_key[256];
    size_t key_len = strlen(websocket_key);
    memcpy(combined_key, websocket_key, key_len);
    memcpy(combined_key + key_len, magic_string, strlen(magic_string));

    // Compute SHA-1 hash
    unsigned char sha1_hash[SHA_DIGEST_LENGTH];
    SHA1((unsigned char*)combined_key, key_len + strlen(magic_string), sha1_hash);

    // Perform Base64 encoding
    char accept_key[256];
    base64_encode(sha1_hash, SHA_DIGEST_LENGTH, accept_key, sizeof(accept_key));

    // Construct WebSocket handshake response
    char response[1024];
    snprintf(response, sizeof(response),
        "HTTP/1.1 101 Switching Protocols\r\n"
        "Upgrade: websocket\r\n"
        "Connection: Upgrade\r\n"
        "Sec-WebSocket-Accept: %s\r\n\r\n",
        accept_key);

    // Send response
    return send(client_socket, response, strlen(response), 0) > 0 ? S_OK : S_FALSE;
}

// WebSocket message handling thread
DWORD WINAPI WebSocketThread(LPVOID lpParam) {
    WSADATA wsaData;
    struct sockaddr_in server_addr, client_addr;
    int client_addr_len = sizeof(client_addr);
    char buffer[WEBSOCKET_BUFFER_SIZE];
    char payload[WEBSOCKET_BUFFER_SIZE];

    // Initialize critical section
    InitializeCriticalSection(&connection_status_mutex);

    // Initialize Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("WSAStartup failed\n");
        return 1;
    }

    // Create socket
    ctx.websocket_server = socket(AF_INET, SOCK_STREAM, 0);
    if (ctx.websocket_server == INVALID_SOCKET) {
        printf("Could not create socket\n");
        WSACleanup();
        return 1;
    }

    // Prepare sockaddr_in structure
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(WEBSOCKET_PORT);

    // Bind
    if (bind(ctx.websocket_server, (struct sockaddr *)&server_addr, sizeof(server_addr)) == SOCKET_ERROR) {
        printf("Bind failed\n");
        closesocket(ctx.websocket_server);
        WSACleanup();
        return 1;
    }

    // Listen
    listen(ctx.websocket_server, 3);
    printf("Waiting for WebSocket connection on port %d...\n", WEBSOCKET_PORT);

    while (websocket_running) {
        // Accept connection
        ctx.websocket_client = accept(ctx.websocket_server, (struct sockaddr *)&client_addr, &client_addr_len);
        if (ctx.websocket_client == INVALID_SOCKET) {
            printf("Accept failed\n");
            update_connection_status(WS_DISCONNECTED);
            continue;
        }

        printf("WebSocket client connected\n");
        update_connection_status(WS_CONNECTING);

        // Receive WebSocket handshake request
        int bytes_received = recv(ctx.websocket_client, buffer, WEBSOCKET_BUFFER_SIZE, 0);
        if (bytes_received > 0) {
            buffer[bytes_received] = '\0';
            printf("Received handshake request: %s\n", buffer);

            // Find Sec-WebSocket-Key
            char* key_start = strstr(buffer, "Sec-WebSocket-Key: ");
            if (key_start) {
                key_start += 19;
                char* key_end = strstr(key_start, "\r\n");
                if (key_end) {
                    *key_end = '\0';
                    // Handle WebSocket handshake
                    if (handle_websocket_handshake(ctx.websocket_client, key_start) == S_OK) {
                        printf("WebSocket handshake successful\n");
                        update_connection_status(WS_CONNECTED);
                    } else {
                        printf("WebSocket handshake failed\n");
                        update_connection_status(WS_DISCONNECTED);
                        continue;
                    }
                }
            }
        }

        // Continuous message receiving loop
        while (websocket_running && ctx.connection_status == WS_CONNECTED) {
            bytes_received = recv(ctx.websocket_client, buffer, WEBSOCKET_BUFFER_SIZE, 0);
            if (bytes_received <= 0) {
                printf("WebSocket connection lost\n");
                update_connection_status(WS_DISCONNECTED);
                break;
            }

            // Decode WebSocket frame
            int payload_len = decode_websocket_frame(buffer, bytes_received, payload, sizeof(payload));
            uint8_t opcode = buffer[0] & 0x0F; // Extract opcode

            if (opcode == WS_OPCODE_CLOSE) {
                printf("WebSocket close frame received.");

                // Update connection status and break
                update_connection_status(WS_DISCONNECTED);
                break;
            } else if (payload_len > 0) {
                printf("Received payload: %s\n", payload);

                // Validate and process card ID
                if (payload_len == AIME_ID_SIZE * 2) {
                    // Convert hex string to bytes
                    for (size_t i = 0; i < AIME_ID_SIZE; i++) {
                        sscanf(payload + i * 2, "%02hhx", &ctx.aime_ids[0][i]);
                    }
                    ctx.current_aime = 0;  // Set current AIME card
                    strncpy(ctx.last_received_card_id, payload, sizeof(ctx.last_received_card_id) - 1);
                    printf("Card ID received: %s\n", ctx.last_received_card_id);
                }
            }
        }

        // Close client connection
        closesocket(ctx.websocket_client);
        update_connection_status(WS_DISCONNECTED);
    }

    closesocket(ctx.websocket_server);
    WSACleanup();
    DeleteCriticalSection(&connection_status_mutex);
    return 0;
}

uint16_t aime_io_get_api_version(void)
{
    return 0x0100;
}

HRESULT aime_io_init(void)
{
    AllocConsole();
    freopen("CONOUT$", "w", stdout);

    ctx.aime_count = GetPrivateProfileIntW(
        L"aime",
        L"aimeCount",
        0,
        CFG_NAME
    );

    printf("using aimeio-multi with %zu aime cards.\n", ctx.aime_count);

    for (size_t i = 0; i < MAX_AIME_CARDS && i < ctx.aime_count; i++)
    {
        wchar_t keyname[32];
        wchar_t aime_id[32];

        memset(aime_id, 0, sizeof(aime_id));

        swprintf(keyname, _countof(keyname), L"aimeId%zu", i);

        GetPrivateProfileStringW(
            L"aime",
            keyname,
            NULL,
            aime_id,
            _countof(aime_id),
            CFG_NAME
        );

        // Convert hex string to byte array
        for (size_t j = 0; j < AIME_ID_SIZE && j < _countof(aime_id) / 2; j++)
        {
            int byte;
            swscanf(aime_id + 2 * j, L"%02x", &byte);
            ctx.aime_ids[i][j] = byte;
        }

        swprintf(keyname, _countof(keyname), L"aimeKey%zu", i);

        ctx.aime_vk[i] = GetPrivateProfileIntW(
            L"aime",
            keyname,
            0,
            CFG_NAME
        );

        printf("Aime %zu:", i);
        for (size_t j = 0; j < AIME_ID_SIZE; j++)
        {
            printf(" %02x", ctx.aime_ids[i][j]);
        }

        printf(", key: 0x%u\n", ctx.aime_vk[i]);
    }

    // Start WebSocket thread
    websocket_running = true;
    websocket_thread = CreateThread(NULL, 0, WebSocketThread, NULL, 0, NULL);
    if (websocket_thread == NULL) {
        printf("Failed to create WebSocket thread\n");
        return S_FALSE;
    }

    printf("aimeio-multi initialized.\n");

    return S_OK;
}

HRESULT aime_io_nfc_poll(uint8_t unit_no)
{
    if (unit_no != 0)
    {
        return S_OK;
    }

    // Check WebSocket received card first
    if (ctx.current_aime >= 0) {
        return S_OK;
    }

    // If no card was received via WebSocket, check keyboard
    for (size_t i = 0; i < ctx.aime_count; i++)
    {
        if (GetAsyncKeyState(ctx.aime_vk[i]) & 0x8000)
        {
            ctx.current_aime = i;
            break;
        }
    }

    return S_OK;
}

HRESULT aime_io_get_websocket_status(int* status) {
    if (!status) return S_FALSE;

    *status = get_connection_status();
    return S_OK;
}

HRESULT aime_io_nfc_get_aime_id(
        uint8_t unit_no,
        uint8_t *luid,
        size_t luid_size)
{
    assert(luid != NULL);

    if (unit_no != 0 || ctx.current_aime < 0)
    {
        return S_FALSE;
    }

    memcpy(luid, ctx.aime_ids[ctx.current_aime], luid_size);

    // Reset current_aime after reading
    ctx.current_aime = -1;

    return S_OK;
}

HRESULT aime_io_nfc_get_felica_id(uint8_t unit_no, uint64_t *IDm)
{
    // FeliCa not supported in aimeio-multi
    return S_FALSE;
}

void aime_io_led_set_color(uint8_t unit_no, uint8_t r, uint8_t g, uint8_t b)
{
    // LED color control implementation
}

// Cleanup function to be called when the program exits
void aime_io_cleanup(void)
{
    websocket_running = false;
    if (websocket_thread) {
        WaitForSingleObject(websocket_thread, INFINITE);
        CloseHandle(websocket_thread);
    }
}