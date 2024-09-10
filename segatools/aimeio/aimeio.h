#pragma once

#include <windows.h>

#include <stddef.h>
#include <stdint.h>

/*
    Get the version of the Aime IO API that this DLL supports. This function
    should return a positive 16-bit integer, where the high byte is the major
    version and the low byte is the minor version (as defined by the Semantic
    Versioning standard).

    The latest API version as of this writing is 0x0100.
 */
uint16_t aime_io_get_api_version(void);

/*
    Initialize Aime IO provider DLL. Only called once, before any other
    functions exported from this DLL are called (except for
    aime_io_get_api_version).

    Minimum API version: 0x0100
 */
HRESULT aime_io_init(void);

/*
    Poll for IC cards in the vicinity.

    - unit_no: Always 0 as of the current API version

    Minimum API version: 0x0100
 */
HRESULT aime_io_nfc_poll(uint8_t unit_no);

/*
    Attempt to read out a classic Aime card ID

    - unit_no: Always 0 as of the current API version
    - luid: Pointer to a ten-byte buffer that will receive the ID
    - luid_size: Size of the buffer at *luid. Always 10.

    Returns:

    - S_OK if a classic Aime is present and was read successfully
    - S_FALSE if no classic Aime card is present (*luid will be ignored)
    - Any HRESULT error if an error occured.

    Minimum API version: 0x0100
*/
HRESULT aime_io_nfc_get_aime_id(
        uint8_t unit_no,
        uint8_t *luid,
        size_t luid_size);

/*
    Attempt to read out a FeliCa card ID ("IDm"). The following are examples
    of FeliCa cards:

    - Amuse IC (which includes new-style Aime-branded cards, among others)
    - Smartphones with FeliCa NFC capability (uncommon outside Japan)
    - Various Japanese e-cash cards and train passes

    Parameters:

    - unit_no: Always 0 as of the current API version
    - IDm: Output parameter that will receive the card ID

    Returns:

    - S_OK if a FeliCa device is present and was read successfully
    - S_FALSE if no FeliCa device is present (*IDm will be ignored)
    - Any HRESULT error if an error occured.

    Minimum API version: 0x0100
*/
HRESULT aime_io_nfc_get_felica_id(uint8_t unit_no, uint64_t *IDm);

/*
    Change the color and brightness of the card reader's RGB lighting

    - unit_no: Always 0 as of the current API version
    - r, g, b: Primary color intensity, from 0 to 255 inclusive.

    Minimum API version: 0x0100
*/
void aime_io_led_set_color(uint8_t unit_no, uint8_t r, uint8_t g, uint8_t b);
