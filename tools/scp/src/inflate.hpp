#pragma once
#include <cstdint>
#include <cstddef>
#include <vector>
#include <string>

namespace scp {
std::vector<uint8_t> inflateRaw(const uint8_t* src, size_t n);
std::vector<uint8_t> packStored(const uint8_t* src, size_t n);
std::string compressScp(const std::string& text);
std::string decompressScp(const uint8_t* data, size_t n);
}
