#include <windows.h>
#include <winsock2.h>
#include <string.h>

#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#define AIME_ID_SIZE 10
#define MAX_AIME_CARDS 16
#define CFG_NAME L".\\segatools.ini"

#include "aimeio/aimeio.h"

struct aimeio_multi_ctx
{
    uint8_t aime_ids[MAX_AIME_CARDS][AIME_ID_SIZE];
    uint8_t aime_vk[MAX_AIME_CARDS];
    size_t aime_count;
    ssize_t current_aime;
};

static struct aimeio_multi_ctx ctx;

static char last_received_card_id[AIME_ID_SIZE * 2 + 1] = {0};  // 用于存储最后接收到的卡号

// 声明 http_server_thread 函数
DWORD WINAPI http_server_thread(LPVOID param);

uint16_t aime_io_get_api_version(void)
{
    return 0x0100;
}

HRESULT aime_io_init(void)
{
    AllocConsole();
    freopen("CONOUT$", "w", stdout);
    // freopen("aimeio-multi.log", "w", stdout);

    printf("Starting HTTP server...\n");

    // 启动 HTTP 服务器线程
    HANDLE server_thread = CreateThread(NULL, 0, http_server_thread, NULL, 0, NULL);
    if (server_thread == NULL) {
        printf("Failed to create HTTP server thread. Error: %lu\n", GetLastError());  // 修改为 %lu
        return E_FAIL;
    } else {
        printf("HTTP server thread created successfully.\n");
    }

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

    printf("aimeio-multi initialized.\n");

    return S_OK;
}

HRESULT aime_io_nfc_poll(uint8_t unit_no)
{
    if (unit_no != 0)
    {
        return S_OK;
    }

    ctx.current_aime = -1;

    // 检查是否有新的卡号通过 HTTP 接收
    if (last_received_card_id[0] != '\0')
    {
        // 将接收到的卡号转换为 aime_id 格式
        uint8_t new_aime_id[AIME_ID_SIZE] = {0};
        for (size_t i = 0; i < AIME_ID_SIZE && i < strlen(last_received_card_id) / 2; i++)
        {
            sscanf(last_received_card_id + 2 * i, "%02hhx", &new_aime_id[i]);
        }

        // 检查是否与现有的 aime_id 匹配
        for (size_t i = 0; i < ctx.aime_count; i++)
        {
            if (memcmp(ctx.aime_ids[i], new_aime_id, AIME_ID_SIZE) == 0)
            {
                ctx.current_aime = i;
                break;
            }
        }

        // 如果没有匹配的 aime_id，添加新的
        if (ctx.current_aime == -1 && ctx.aime_count < MAX_AIME_CARDS)
        {
            memcpy(ctx.aime_ids[ctx.aime_count], new_aime_id, AIME_ID_SIZE);
            ctx.current_aime = ctx.aime_count;
            ctx.aime_count++;
        }

        // 清除已处理的卡号
        memset(last_received_card_id, 0, sizeof(last_received_card_id));
    }
    else
    {
        // 原有的按键检查逻辑
        for (size_t i = 0; i < ctx.aime_count; i++)
        {
            if (GetAsyncKeyState(ctx.aime_vk[i]) & 0x8000)
            {
                ctx.current_aime = i;
                break;
            }
        }
    }

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

    return S_OK;
}

HRESULT aime_io_nfc_get_felica_id(uint8_t unit_no, uint64_t *IDm)
{
    // FeliCa not supported in aimeio-multi

    return S_FALSE;
}

void aime_io_led_set_color(uint8_t unit_no, uint8_t r, uint8_t g, uint8_t b)
{}

DWORD WINAPI http_server_thread(LPVOID param) {
    WSADATA wsa;
    SOCKET server_socket, client_socket;
    struct sockaddr_in server, client;
    int c;
    char client_message[2000];

    printf("Initializing WinSock...\n");
    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) {
        printf("Failed. Error Code: %d\n", WSAGetLastError());
        return 1;
    }

    printf("WinSock initialized.\n");

    if ((server_socket = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET) {
        printf("Could not create socket: %d\n", WSAGetLastError());
        return 1;
    }
    printf("Socket created.\n");

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(8080);

    if (bind(server_socket, (struct sockaddr*)&server, sizeof(server)) == SOCKET_ERROR) {
        printf("Bind failed. Error Code: %d\n", WSAGetLastError());
        closesocket(server_socket);
        return 1;
    }
    printf("Bind done.\n");

    listen(server_socket, 3);

    printf("Waiting for incoming connections on port 8080...\n");
    c = sizeof(struct sockaddr_in);

    while ((client_socket = accept(server_socket, (struct sockaddr*)&client, &c)) != INVALID_SOCKET) {
        printf("Connection accepted.\n");

        int recv_size = recv(client_socket, client_message, 1999, 0);
        if (recv_size == SOCKET_ERROR) {
            printf("Recv failed. Error Code: %d\n", WSAGetLastError());
            closesocket(client_socket);
            continue;
        }
        client_message[recv_size] = '\0';
        printf("Received message: %s\n", client_message);

        // Parse the URL for card number
        char* card_number = NULL;
        if (strstr(client_message, "GET /cardnumber") != NULL) {
            char* value_param = strstr(client_message, "value=");
            if (value_param != NULL) {
                value_param += 6; // Move past "value="
                char* end = strchr(value_param, ' ');
                if (end != NULL) {
                    *end = '\0';
                    card_number = value_param;
                    printf("Received Card ID: %s\n", card_number);

                    // 存储接收到的卡号
                    strncpy(last_received_card_id, card_number, sizeof(last_received_card_id) - 1);
                    last_received_card_id[sizeof(last_received_card_id) - 1] = '\0';
                }
            }

            char response[256];
            if (card_number != NULL && strlen(card_number) > 0) {
                snprintf(response, sizeof(response),
                    "HTTP/1.1 200 OK\r\nContent-Length: %d\r\n\r\nCard ID received: %s\r\n",
                    (int)strlen(card_number) + 19, card_number);
            } else {
                const char* msg = "No card number provided";
                snprintf(response, sizeof(response),
                    "HTTP/1.1 400 Bad Request\r\nContent-Length: %d\r\n\r\n%s\r\n",
                    (int)strlen(msg), msg);
            }
            send(client_socket, response, strlen(response), 0);
        } else {
            const char* msg = "Not Found";
            char response[256];
            snprintf(response, sizeof(response),
                "HTTP/1.1 404 Not Found\r\nContent-Length: %d\r\n\r\n%s\r\n",
                (int)strlen(msg), msg);
            send(client_socket, response, strlen(response), 0);
        }

        closesocket(client_socket);
        printf("Connection closed.\n");
    }

    if (client_socket == INVALID_SOCKET) {
        printf("Accept failed. Error Code: %d\n", WSAGetLastError());
        closesocket(server_socket);
        return 1;
    }

    closesocket(server_socket);
    WSACleanup();
    return 0;
}