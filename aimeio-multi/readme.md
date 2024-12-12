aimeio-multi
---

Aime IO DLL for `segatools` supporting mutiple (up to 16) Aime cards.

### Usage

Copy `aimeio.dll` to your `segatools` folder and add the following to your `segatools.ini`:

```ini
[aime]
; enable segatools aime emulation
enable=1

; number of aime cards
aimeCount=2

; set aime card IDs (access code) and keys to activate them with aimeId{n} and aimeKey{n} (0-indexed)
; for key codes, check the following:
; https://learn.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes

aimeId0=12345123451234512345
aimeKey0=0x0D

aimeId1=54321543215432154321
aimeKey1=0x10

[aimeio]
path=aimeio.dll
```

### Build

On Linux:

```sh
meson setup --cross cross-mingw-32.txt b32
ninja -C b32
meson setup --cross cross-mingw-64.txt b64
ninja -C b64
```