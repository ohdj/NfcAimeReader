#include <windows.h>

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

uint16_t aime_io_get_api_version(void)
{
    return 0x0100;
}

HRESULT aime_io_init(void)
{
    AllocConsole();
    freopen("CONOUT$", "w", stdout);
    // freopen("aimeio-multi.log", "w", stdout);

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
