cmake_minimum_required(VERSION 3.22.1)

project(llama_jni)

set(CMAKE_CXX_STANDARD 17)

add_library(llama_jni SHARED
    llama_jni.cpp
)

find_library(log-lib log)

target_link_libraries(llama_jni
    ${log-lib}
)