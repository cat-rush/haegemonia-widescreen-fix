#include "inflate.hpp"
#include <cstring>
#include <stdexcept>

namespace scp {

namespace {

struct BitReader {
    const uint8_t* p;
    size_t len;
    size_t pos;
    uint32_t buf;
    int nbits;
    BitReader(const uint8_t* d, size_t n) : p(d), len(n), pos(0), buf(0), nbits(0) {}
    uint32_t peek(int n) {
        while (nbits < n && pos < len) { buf |= (uint32_t)p[pos++] << nbits; nbits += 8; }
        return buf & ((1u << n) - 1);
    }
    void drop(int n) {
        buf >>= n; nbits -= n;
    }
    uint32_t get(int n) { uint32_t v = peek(n); drop(n); return v; }
    bool aligned() { return nbits % 8 == 0; }
};

struct Huffman {
    uint16_t count[16];
    uint16_t symbol[288];
    int maxbits;
    Huffman() : maxbits(0) { std::memset(count, 0, sizeof(count)); }
    bool build(const uint8_t* lengths, int n) {
        std::memset(count, 0, sizeof(count));
        for (int i = 0; i < n; i++) {
            if (lengths[i] > 15) return false;
            count[lengths[i]]++;
        }
        count[0] = 0;
        uint16_t offs[16];
        uint16_t off = 0;
        maxbits = 0;
        for (int i = 1; i < 16; i++) {
            offs[i] = off;
            off += count[i];
            if (count[i]) maxbits = i;
        }
        if (off == 0) return true;
        for (int i = 0; i < n; i++) {
            if (lengths[i]) symbol[offs[lengths[i]]++] = (uint16_t)i;
        }
        return true;
    }
    int decode(BitReader& br) const {
        int code = 0, first = 0, index = 0;
        for (int len = 1; len <= maxbits; len++) {
            code |= (int)br.get(1);
            int cnt = count[len];
            if (code - first < cnt) return symbol[index + (code - first)];
            index += cnt;
            first += cnt;
            first <<= 1;
            code <<= 1;
        }
        return -1;
    }
};

const uint8_t kOrder[19] = {16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15};
const uint16_t kLens[29] = {3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258};
const uint16_t kLeb[29] = {0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0};
const uint16_t kDbase[30] = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};
const uint16_t kDeb[30] = {0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13};

}

std::vector<uint8_t> inflateRaw(const uint8_t* src, size_t n) {
    BitReader br(src, n);
    std::vector<uint8_t> out;
    out.reserve(n * 4);
    uint8_t litlens[288];
    uint8_t dists[32];
    Huffman lit, dist;
    bool last = false;
    while (!last) {
        last = br.get(1) != 0;
        uint32_t type = br.get(2);
        if (type == 0) {
            br.drop(br.nbits % 8);
            uint32_t len = br.get(16);
            uint32_t nlen = br.get(16);
            if ((len ^ 0xFFFF) != nlen) throw std::runtime_error("bad stored block header");
            if (br.pos + len > br.len) throw std::runtime_error("stored block overrun");
            out.insert(out.end(), br.p + br.pos, br.p + br.pos + len);
            br.pos += len;
            continue;
        }
        if (type == 1) {
            for (int i = 0; i < 144; i++) litlens[i] = 8;
            for (int i = 144; i < 256; i++) litlens[i] = 9;
            for (int i = 256; i < 280; i++) litlens[i] = 7;
            for (int i = 280; i < 288; i++) litlens[i] = 8;
            for (int i = 0; i < 32; i++) dists[i] = 5;
            lit.build(litlens, 288);
            dist.build(dists, 32);
        } else {
            uint32_t hlit = br.get(5) + 257;
            uint32_t hdist = br.get(5) + 1;
            uint32_t hclen = br.get(4) + 4;
            uint8_t clens[19] = {0};
            for (uint32_t i = 0; i < hclen; i++) clens[kOrder[i]] = (uint8_t)br.get(3);
            Huffman clen;
            if (!clen.build(clens, 19)) throw std::runtime_error("bad code lengths");
            int total = hlit + hdist;
            int i = 0;
            uint8_t all[288 + 32];
            while (i < total) {
                int sym = clen.decode(br);
                if (sym < 0) throw std::runtime_error("bad codelen symbol");
                if (sym < 16) { all[i++] = (uint8_t)sym; }
                else if (sym == 16) {
                    int rep = 3 + br.get(2);
                    if (i == 0) throw std::runtime_error("repeat with no prev");
                    uint8_t prev = all[i - 1];
                    while (rep-- && i < total) all[i++] = prev;
                } else if (sym == 17) {
                    int rep = 3 + br.get(3);
                    while (rep-- && i < total) all[i++] = 0;
                } else {
                    int rep = 11 + br.get(7);
                    while (rep-- && i < total) all[i++] = 0;
                }
            }
            if (!lit.build(all, hlit)) throw std::runtime_error("bad lit huffman");
            if (!dist.build(all + hlit, hdist)) throw std::runtime_error("bad dist huffman");
        }
        for (;;) {
            int sym = lit.decode(br);
            if (sym < 0) throw std::runtime_error("bad literal symbol");
            if (sym < 256) {
                out.push_back((uint8_t)sym);
            } else if (sym == 256) {
                break;
            } else {
                int li = sym - 257;
                if (li >= 29) throw std::runtime_error("bad length symbol");
                int len = kLens[li] + br.get(kLeb[li]);
                int dsym = dist.decode(br);
                if (dsym < 0 || dsym >= 30) throw std::runtime_error("bad distance symbol");
                int d = kDbase[dsym] + br.get(kDeb[dsym]);
                if (d > (int)out.size()) throw std::runtime_error("distance too far");
                for (int k = 0; k < len; k++) out.push_back(out[out.size() - d]);
            }
        }
    }
    return out;
}

std::vector<uint8_t> packStored(const uint8_t* src, size_t n) {
    std::vector<uint8_t> out;
    out.reserve(n + n / 65535 * 5 + 5);
    size_t off = 0;
    while (off < n) {
        size_t chunk = n - off;
        bool last = false;
        if (chunk > 65535) chunk = 65535;
        else last = true;
        out.push_back(last ? 0x01 : 0x00);
        uint16_t len = (uint16_t)chunk;
        out.push_back((uint8_t)(len & 0xFF));
        out.push_back((uint8_t)(len >> 8));
        uint16_t nlen = (uint16_t)(~len);
        out.push_back((uint8_t)(nlen & 0xFF));
        out.push_back((uint8_t)(nlen >> 8));
        out.insert(out.end(), src + off, src + off + chunk);
        off += chunk;
    }
    return out;
}

std::string decompressScp(const uint8_t* data, size_t n) {
    if (n < 6) throw std::runtime_error("file too small");
    uint32_t declared = (uint32_t)data[0] | ((uint32_t)data[1] << 8) | ((uint32_t)data[2] << 16) | ((uint32_t)data[3] << 24);
    if (data[4] == 0x78 && data[5] == 0xDA) {
        std::vector<uint8_t> out = inflateRaw(data + 6, n - 6);
        if (declared && out.size() != declared) {
        }
        return std::string((const char*)out.data(), out.size());
    }
    return std::string((const char*)(data + 4), n - 4);
}

std::string compressScp(const std::string& text) {
    std::string out;
    uint32_t size = (uint32_t)text.size();
    out.push_back((char)(size & 0xFF));
    out.push_back((char)((size >> 8) & 0xFF));
    out.push_back((char)((size >> 16) & 0xFF));
    out.push_back((char)((size >> 24) & 0xFF));
    out.push_back((char)0x78);
    out.push_back((char)0xDA);
    std::vector<uint8_t> packed = packStored((const uint8_t*)text.data(), text.size());
    out.append((const char*)packed.data(), packed.size());
    /* zlib ADLER32 trailer: the game's zlib inflate REQUIRES it -
       without it the game logs "ZLib Error" and the script fails to
       load (that is why both text and packed saves broke e5m5). */
    uint32_t a = 1, b = 0;
    for (size_t i = 0; i < text.size(); i++) {
        a = (a + (uint8_t)text[i]) % 65521u;
        b = (b + a) % 65521u;
    }
    uint32_t ad = (b << 16) | a;
    out.push_back((char)(ad >> 24));
    out.push_back((char)(ad >> 16));
    out.push_back((char)(ad >> 8));
    out.push_back((char)ad);
    return out;
}

}
