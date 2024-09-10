project(
    'segatools',
    'c',
    version: '0.1.0',
    default_options: [
        'werror=true',
    ],
)

add_project_arguments(
    '-DCOBJMACROS',
    '-DDIRECTINPUT_VERSION=0x0800',
    '-DWIN32_LEAN_AND_MEAN',
    '-D_WIN32_WINNT=_WIN32_WINNT_WIN7',
    '-DMINGW_HAS_SECURE_API=1',
    '-Wno-unused',
    # '-ggdb',  # Add debug information
    language: 'c',
)

cc = meson.get_compiler('c')

if cc.get_id() != 'msvc'
    add_project_arguments(
        '-ffunction-sections',
        '-fdata-sections',
        language: 'c',
    )

    add_project_link_arguments(
        '-Wl,--enable-stdcall-fixup',
        '-Wl,--exclude-all-symbols',
        '-Wl,--gc-sections',
        '-static-libgcc',
        # '-ggdb',  # Add debug information
        '-lcrypt32', # Bcrypt needed for prashook
        # '-Wl,-s',  # Strip debug symbols
        language: 'c',
    )
endif

if get_option('log_all') or get_option('log_jvs')
    add_project_arguments('-DLOG_JVS', language: 'c')
endif
if get_option('log_all') or get_option('log_io3')
    add_project_arguments('-DLOG_IO3', language: 'c')
endif
if get_option('log_all') or get_option('log_led15093')
    add_project_arguments('-DLOG_LED15093', language: 'c')
endif
if get_option('log_all') or get_option('log_nfc')
    add_project_arguments('-DLOG_NFC', language: 'c')
endif
if get_option('log_all') or get_option('log_carol_control_bd')
    add_project_arguments('-DLOG_CAROL_CONTROL_BD', language: 'c')
endif
if get_option('log_all') or get_option('log_carol_led_bd')
    add_project_arguments('-DLOG_CAROL_LED_BD', language: 'c')
endif
if get_option('log_all') or get_option('log_carol_touch')
    add_project_arguments('-DLOG_CAROL_TOUCH', language: 'c')
endif
if get_option('log_all') or get_option('log_chuni_slider')
    add_project_arguments('-DLOG_CHUNI_SLIDER', language: 'c')
endif
if get_option('log_all') or get_option('log_chusan_slider')
    add_project_arguments('-DLOG_CHUSAN_SLIDER', language: 'c')
endif
if get_option('log_all') or get_option('log_diva_slider')
    add_project_arguments('-DLOG_DIVA_SLIDER', language: 'c')
endif
if get_option('log_all') or get_option('log_mercury_slider')
    add_project_arguments('-DLOG_MERCURY_SLIDER', language: 'c')
endif
if get_option('log_all') or get_option('log_clock')
    add_project_arguments('-DLOG_CLOCK', language: 'c')
endif

shlwapi_lib = cc.find_library('shlwapi')
dinput8_lib = cc.find_library('dinput8')
dxguid_lib = cc.find_library('dxguid')
xinput_lib = cc.find_library('xinput')
pathcch_lib = cc.find_library('pathcch')

inc = include_directories('.')
capnhook = subproject('capnhook')

subdir('amex')
subdir('iccard')
subdir('board')
subdir('hooklib')
subdir('jvs')
subdir('platform')
subdir('util')

subdir('gfxhook')
subdir('unityhook')

subdir('aimeio')
subdir('chuniio')
subdir('divaio')
subdir('carolio')
subdir('idzio')
subdir('idacio')
subdir('swdcio')
subdir('mu3io')
subdir('mai2io')
subdir('cmio')
subdir('mercuryio')
subdir('cxbio')
subdir('tokyoio')
subdir('fgoio')

subdir('chunihook')
subdir('divahook')
subdir('carolhook')
subdir('idzhook')
subdir('idachook')
subdir('swdchook')
subdir('minihook')
subdir('chusanhook')
subdir('mu3hook')
subdir('mai2hook')
subdir('cmhook')
subdir('mercuryhook')
subdir('cxbhook')
subdir('tokyohook')
subdir('fgohook')
