cmake_minimum_required(VERSION 3.10)
project(libmaliopt)

# Configuração para ARM64 (TECNO KH7)
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR aarch64)

# Compilação para Android (Termux)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC -O2 -std=c++17")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -O2 -std=c11")

# Fontes
add_library(maliopt SHARED
    src/main/java/com/tdbr/optimizer/detector/NativeExtensionDetector.cpp
)

# Opções de compilação
target_compile_options(maliopt PRIVATE -fPIC -O2)
target_link_options(maliopt PRIVATE -ldl -llog)

# Instalação
set_target_properties(maliopt PROPERTIES
    OUTPUT_NAME "libmaliopt"
    SUFFIX ".so"
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib/aarch64
)