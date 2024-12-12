project(
    'aimeio-multi',
    'c',
    version: '0.0.1',
    default_options: [
        'warning_level=3',
    ],
)

add_project_arguments(
    '-DCOBJMACROS',
    '-DDIRECTINPUT_VERSION=0x0800',
    '-DWIN32_LEAN_AND_MEAN',
    '-D_WIN32_WINNT=_WIN32_WINNT_WIN7',
    '-DMINGW_HAS_SECURE_API=1',
    '-Wno-unused',
    language: 'c',
)

cc = meson.get_compiler('c')

if cc.get_id() != 'msvc'
    add_project_arguments(
        '-ffunction-sections',
        '-fdata-sections',
        '-flto',  # Enable Link-Time Optimization
        language: 'c',
    )

    add_project_link_arguments(
        '-Wl,--enable-stdcall-fixup',
        '-Wl,--exclude-all-symbols',
        '-Wl,--gc-sections',
        '-static-libgcc',
        '-flto',  # Enable Link-Time Optimization
        '-Wl,-s',  # Strip debug symbols
        language: 'c',
    )
endif

inc = include_directories('.')

subdir('aimeio')
