aimeio_lib = shared_library(
    'aimeio',
    name_prefix : '',
    include_directories: inc,
    implicit_include_directories : false,
    vs_module_defs : 'aimeio.def',
    sources : [
        'aimeio.c',
    ],
    dependencies : [
        meson.get_compiler('c').find_library('ws2_32'),
    ],
)