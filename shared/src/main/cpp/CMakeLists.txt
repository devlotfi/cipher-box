cmake_minimum_required(VERSION 3.22.1)

project("shared")

# --- tinyproto: find its sources and headers ---
set(TP_SRC ${CMAKE_CURRENT_SOURCE_DIR}/tinyproto/src)

file(GLOB_RECURSE TP_SOURCES ${TP_SRC}/*.c ${TP_SRC}/*.cpp)
list(FILTER TP_SOURCES EXCLUDE REGEX ".*/(esp32|arduino|win32|avr)/.*")  # other platforms

file(GLOB_RECURSE TP_HEADERS ${TP_SRC}/*.h)
foreach (h ${TP_HEADERS})
  get_filename_component(d ${h} DIRECTORY)
  list(APPEND TP_INCLUDE_DIRS ${d})
endforeach ()
list(REMOVE_DUPLICATES TP_INCLUDE_DIRS)
include_directories(${TP_INCLUDE_DIRS})

# --- your library: JNI file + tinyproto sources ---
add_library(${CMAKE_PROJECT_NAME} SHARED
        shared.cpp
        ${TP_SOURCES})

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log)