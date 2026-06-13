cmake_minimum_required(VERSION 3.18)

project(fcitx5-android-plugin-avro VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

include("${FCITX_INSTALL_CMAKECONFIG_DIR}/Fcitx5Utils/Fcitx5CompilerSettings.cmake")

add_fcitx5_addon(avro
    avro.cpp
    avro.h
    avro_parser.cpp
    avro_parser.h
)

target_link_libraries(avro PRIVATE Fcitx5::Core)

fcitx5_translate_desktop_file(avro.conf.in avro.conf)
fcitx5_translate_desktop_file(probhat.conf.in probhat.conf)
configure_file(avro-addon.conf.in.in avro-addon.conf.in COPYONLY)
fcitx5_translate_desktop_file("${CMAKE_CURRENT_BINARY_DIR}/avro-addon.conf.in" avro-addon.conf)

install(TARGETS avro DESTINATION "${CMAKE_INSTALL_LIBDIR}/fcitx5" COMPONENT translation)
install(FILES "${CMAKE_CURRENT_BINARY_DIR}/avro.conf"
        "${CMAKE_CURRENT_BINARY_DIR}/probhat.conf"
        DESTINATION "${FCITX_INSTALL_PKGDATADIR}/inputmethod" COMPONENT config)
install(FILES "${CMAKE_CURRENT_BINARY_DIR}/avro-addon.conf" RENAME avro.conf
        DESTINATION "${FCITX_INSTALL_PKGDATADIR}/addon" COMPONENT config)
