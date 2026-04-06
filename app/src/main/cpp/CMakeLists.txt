cmake_minimum_required(VERSION 3.22.1)

project("doomllm")

add_library(${CMAKE_PROJECT_NAME} SHARED
        llm_mnn_jni.cpp
        llm_session.cpp)

target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")

set(MNN_SOURCE_ROOT "D:/dev/MNN")
set(MNN_LIB_PATH "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libMNN.so")

include_directories("${MNN_SOURCE_ROOT}/include")
include_directories("${MNN_SOURCE_ROOT}/transformers/llm/engine/include")
include_directories("${MNN_SOURCE_ROOT}/3rd_party")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/third_party")

add_library(MNN SHARED IMPORTED)
set_target_properties(MNN PROPERTIES IMPORTED_LOCATION "${MNN_LIB_PATH}")

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log
        MNN)
