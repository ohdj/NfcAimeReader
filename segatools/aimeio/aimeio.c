#include <windows.h>

#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "aimeio/aimeio.h"

#include "util/crc.h"
#include "util/dprintf.h"

struct aime_io_config {
    wchar_t aime_path[MAX_PATH];
    wchar_t felica_path[MAX_PATH];
    bool felica_gen;
    bool aime_gen;
    uint8_t vk_scan;
};

static struct aime_io_config aime_io_cfg;
static uint8_t aime_io_aime_id[10];
static uint8_t aime_io_felica_id[8];
static bool aime_io_aime_id_present;
static bool aime_io_felica_id_present;

static void aime_io_config_read(
        struct aime_io_config *cfg,
        const wchar_t *filename);

static HRESULT aime_io_read_id_file(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes);

static HRESULT aime_io_generate_felica(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes);

static HRESULT aime_io_generate_aime(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes);

static void aime_io_config_read(
        struct aime_io_config *cfg,
        const wchar_t *filename)
{
    assert(cfg != NULL);
    assert(filename != NULL);

    GetPrivateProfileStringW(
            L"aime",
            L"aimePath",
            L"DEVICE\\aime.txt",
            cfg->aime_path,
            _countof(cfg->aime_path),
            filename);

    GetPrivateProfileStringW(
            L"aime",
            L"felicaPath",
            L"DEVICE\\felica.txt",
            cfg->felica_path,
            _countof(cfg->felica_path),
            filename);

    cfg->felica_gen = GetPrivateProfileIntW(
            L"aime",
            L"felicaGen",
            0,
            filename);

    cfg->aime_gen = GetPrivateProfileIntW(
            L"aime",
            L"aimeGen",
            1,
            filename);

    cfg->vk_scan = GetPrivateProfileIntW(
            L"aime",
            L"scan",
            VK_RETURN,
            filename);
}

static HRESULT aime_io_read_id_file(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes)
{
    HRESULT hr;
    FILE *f;
    size_t i;
    int byte;
    int r;

    f = _wfopen(path, L"r");

    if (f == NULL) {
        return S_FALSE;
    }

    memset(bytes, 0, nbytes);

    for (i = 0 ; i < nbytes ; i++) {
        r = fscanf(f, "%02x ", &byte);

        if (r != 1) {
            hr = E_FAIL;
            dprintf("AimeIO DLL: %S: fscanf[%i] failed: %i\n",
                    path,
                    (int) i,
                    r);

            goto end;
        }

        bytes[i] = byte;
    }

    hr = S_OK;

end:
    if (f != NULL) {
        fclose(f);
    }

    return hr;
}

static HRESULT aime_io_generate_felica(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes)
{
    size_t i;
    FILE *f;

    assert(path != NULL);
    assert(bytes != NULL);
    assert(nbytes > 0);

    srand(time(NULL));

    for (i = 0; i < nbytes; i++) {
        bytes[i] = rand();
    }

    /* FeliCa IDm values should have a 0 in their high nibble. I think. */
    bytes[0] &= 0x0F;

    f = _wfopen(path, L"w");

    if (f == NULL) {
        dprintf("AimeIO DLL: %S: fopen failed: %i\n", path, (int) errno);

        return E_FAIL;
    }

    for (i = 0; i < nbytes; i++) {
        fprintf(f, "%02X", bytes[i]);
    }

    fprintf(f, "\n");
    fclose(f);

    dprintf("AimeIO DLL: Generated random FeliCa ID\n");

    return S_OK;
}

static HRESULT aime_io_generate_aime(
        const wchar_t *path,
        uint8_t *bytes,
        size_t nbytes)
{
    size_t i;
    FILE *f;

    assert(path != NULL);
    assert(bytes != NULL);
    assert(nbytes > 0);

    srand(time(NULL));

    /* AiMe IDs should not start with 3, due to a missing check for BananaPass IDs */
    do {
        for (i = 0; i < nbytes; i++) {
            bytes[i] = rand() % 10 << 4 | rand() % 10;
        }
    } while (bytes[0] >> 4 == 3);

    f = _wfopen(path, L"w");

    if (f == NULL) {
        dprintf("AimeIO DLL: %S: fopen failed: %i\n", path, (int) errno);

        return E_FAIL;
    }

    for (i = 0; i < nbytes; i++) {
        fprintf(f, "%02x", bytes[i]);
    }

    fprintf(f, "\n");
    fclose(f);

    dprintf("AimeIO DLL: Generated random AiMe ID\n");

    return S_OK;
}

uint16_t aime_io_get_api_version(void)
{
    return 0x0100;
}

HRESULT aime_io_init(void)
{
    aime_io_config_read(&aime_io_cfg, L".\\segatools.ini");

    return S_OK;
}

HRESULT aime_io_nfc_poll(uint8_t unit_no)
{
    bool sense;
    HRESULT hr;

    if (unit_no != 0) {
        return S_OK;
    }

    /* Reset presence flags */

    aime_io_aime_id_present = false;
    aime_io_felica_id_present = false;

    /* Don't do anything more if the scan key is not held */

    sense = GetAsyncKeyState(aime_io_cfg.vk_scan) & 0x8000;

    if (!sense) {
        return S_OK;
    }

    /* Try AiMe IC */

    hr = aime_io_read_id_file(
            aime_io_cfg.aime_path,
            aime_io_aime_id,
            sizeof(aime_io_aime_id));

    if (SUCCEEDED(hr) && hr != S_FALSE) {
        aime_io_aime_id_present = true;

        return S_OK;
    }

    /* Try generating AiMe IC (if enabled) */

    if (aime_io_cfg.aime_gen) {
        hr = aime_io_generate_aime(
                aime_io_cfg.aime_path,
                aime_io_aime_id,
                sizeof(aime_io_aime_id));

        if (FAILED(hr)) {
            return hr;
        }

        aime_io_aime_id_present = true;
        return S_OK;
    }

    /* Try FeliCa IC */

    hr = aime_io_read_id_file(
            aime_io_cfg.felica_path,
            aime_io_felica_id,
            sizeof(aime_io_felica_id));

    if (SUCCEEDED(hr) && hr != S_FALSE) {
        aime_io_felica_id_present = true;

        return S_OK;
    }

    /* Try generating FeliCa IC (if enabled) */

    if (aime_io_cfg.felica_gen) {
        hr = aime_io_generate_felica(
                aime_io_cfg.felica_path,
                aime_io_felica_id,
                sizeof(aime_io_felica_id));

        if (FAILED(hr)) {
            return hr;
        }

        aime_io_felica_id_present = true;
    }

    return S_OK;
}

HRESULT aime_io_nfc_get_aime_id(
        uint8_t unit_no,
        uint8_t *luid,
        size_t luid_size)
{
    assert(luid != NULL);
    assert(luid_size == sizeof(aime_io_aime_id));

    if (unit_no != 0 || !aime_io_aime_id_present) {
        return S_FALSE;
    }

    memcpy(luid, aime_io_aime_id, luid_size);

    return S_OK;
}

HRESULT aime_io_nfc_get_felica_id(uint8_t unit_no, uint64_t *IDm)
{
    uint64_t val;
    size_t i;

    assert(IDm != NULL);

    if (unit_no != 0 || !aime_io_felica_id_present) {
        return S_FALSE;
    }

    val = 0;

    for (i = 0 ; i < 8 ; i++) {
        val = (val << 8) | aime_io_felica_id[i];
    }

    *IDm = val;

    return S_OK;
}

void aime_io_led_set_color(uint8_t unit_no, uint8_t r, uint8_t g, uint8_t b)
{}
