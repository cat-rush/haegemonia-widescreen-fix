#include "model.hpp"
#include "inflate.hpp"
#include <fstream>
#include <sstream>
#include <algorithm>
#include <cstring>

namespace scp {

static const char* kEntities[5][2] = {
    {"&amp;", "&"}, {"&lt;", "<"}, {"&gt;", ">"}, {"&quot;", "\""}, {"&apos;", "'"}
};

std::string xmlEscape(const std::string& s) {
    std::string o;
    o.reserve(s.size() + 8);
    for (char c : s) {
        switch (c) {
            case '&': o += "&amp;"; break;
            case '<': o += "&lt;"; break;
            case '>': o += "&gt;"; break;
            case '"': o += "&quot;"; break;
            case '\'': o += "&apos;"; break;
            default: o += c;
        }
    }
    return o;
}

std::string xmlUnescape(const std::string& s) {
    std::string o;
    o.reserve(s.size());
    size_t i = 0;
    while (i < s.size()) {
        if (s[i] == '&') {
            bool done = false;
            for (auto& e : kEntities) {
                size_t n = std::strlen(e[0]);
                if (s.compare(i, n, e[0]) == 0) { o += e[1]; i += n; done = true; break; }
            }
            if (!done) { o += '&'; i++; }
        } else {
            o += s[i++];
        }
    }
    return o;
}

Node* parseXml(const std::string& text) {
    struct P {
        const std::string& t;
        size_t i = 0;
        P(const std::string& s) : t(s) {}
        void ws() { while (i < t.size() && (t[i] == ' ' || t[i] == '\t' || t[i] == '\r' || t[i] == '\n')) i++; }
        bool eof() { return i >= t.size(); }
        static bool nameStart(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == ':'; }
        std::string readName() {
            size_t s = i;
            while (i < t.size() && t[i] != '>' && t[i] != '/' && t[i] != '=' &&
                   t[i] != ' ' && t[i] != '\t' && t[i] != '\r' && t[i] != '\n') i++;
            return t.substr(s, i - s);
        }
        Node* parseElement(Node* parent) {
            i++;
            std::string tag = readName();
            Node* n = new Node(tag, parent);
            if (parent) parent->children.push_back(n);
            for (;;) {
                ws();
                if (!eof() && t[i] == '/' && i + 1 < t.size() && t[i + 1] == '>') { i += 2; return n; }
                if (!eof() && t[i] == '>') { i++; break; }
                if (eof()) return n;
                std::string an = readName();
                if (an.empty()) return n;
                ws();
                if (!eof() && t[i] == '=') {
                    i++;
                    ws();
                    if (!eof() && (t[i] == '"' || t[i] == '\'')) {
                        char q = t[i++];
                        std::string v;
                        while (i < t.size() && t[i] != q) v += t[i++];
                        if (i < t.size()) i++;
                        n->attrs.emplace_back(an, xmlUnescape(v));
                    } else {
                        n->attrs.emplace_back(an, "");
                    }
                } else {
                    n->attrs.emplace_back(an, "");
                }
            }
            for (;;) {
                if (i + 1 < t.size() && t[i] == '<' && t[i + 1] == '/') {
                    i += 2;
                    std::string end = readName();
                    ws();
                    if (!eof() && t[i] == '>') i++;
                    trimText(n);
                    return n;
                }
                if (eof()) { trimText(n); return n; }
                if (t.compare(i, 4, "<!--") == 0) {
                    size_t e = t.find("-->", i);
                    i = e == std::string::npos ? t.size() : e + 3;
                    continue;
                }
                if (t[i] == '<' && i + 1 < t.size() && nameStart(t[i + 1])) {
                    parseElement(n);
                    continue;
                }
                std::string chunk;
                while (i < t.size() && t[i] != '<') chunk += t[i++];
                n->text += chunk;
            }
        }
        static void trimText(Node* n) {
            size_t a = 0, b = n->text.size();
            while (a < b && (n->text[a] == ' ' || n->text[a] == '\t' || n->text[a] == '\r' || n->text[a] == '\n')) a++;
            while (b > a && (n->text[b - 1] == ' ' || n->text[b - 1] == '\t' || n->text[b - 1] == '\r' || n->text[b - 1] == '\n')) b--;
            n->text = xmlUnescape(n->text.substr(a, b - a));
        }
    };
    P p(text);
    p.ws();
    Node* root = nullptr;
    while (!p.eof()) {
        if (p.t[p.i] == '<' && p.t.compare(p.i, 4, "<!--") == 0) {
            size_t e = p.t.find("-->", p.i);
            p.i = e == std::string::npos ? p.t.size() : e + 3;
            continue;
        }
        if (p.t[p.i] == '<') {
            root = p.parseElement(nullptr);
        } else {
            p.i++;
        }
    }
    if (!root) throw std::runtime_error("empty document");
    return root;
}

static std::string sanitizeXml(const std::string& s) {
    /* strip NULs and control chars - a NUL inside an attribute value
       ("All\0") breaks the game's script parser entirely (the e5m5
       mission bug); this is the last-line defense on save. */
    std::string o;
    o.reserve(s.size());
    for (char c : s) {
        if ((unsigned char)c >= 0x20 || c == '\t' || c == '\n' || c == '\r') o += c;
    }
    return o;
}

static void writeNode(std::string& o, Node* n) {
    o += '<';
    o += n->tag;
    for (auto& a : n->attrs) {
        o += ' ';
        o += a.first;
        o += "=\"";
        o += xmlEscape(sanitizeXml(a.second));
        o += '"';
    }
    o += ">\n";
    if (!n->text.empty()) o += xmlEscape(sanitizeXml(n->text));
    for (size_t i = 0; i < n->children.size(); i++) {
        if (i > 0) o += '\n';
        writeNode(o, n->children[i]);
    }
    if (!n->children.empty()) o += '\n';
    o += "</";
    o += n->tag;
    o += '>';
}

std::string serializeXml(Node* root) {
    std::string o;
    writeNode(o, root);
    o += '\n';
    return o;
}

std::string loadScpFile(const std::string& path) {
    std::ifstream f(path, std::ios::binary);
    if (!f) throw std::runtime_error("cannot open file");
    std::stringstream ss;
    ss << f.rdbuf();
    std::string c = ss.str();
    if (looksPacked(c)) {
        try {
            return decompressScp((const uint8_t*)c.data(), c.size());
        } catch (std::exception&) {
            throw std::runtime_error("packed file but decompression failed");
        }
    }
    return c;
}

bool looksPacked(const std::string& content) {
    if (content.size() < 6) return false;
    const uint8_t* d = (const uint8_t*)content.data();
    return d[4] == 0x78 && d[5] == 0xDA;
}

void saveScpFile(const std::string& path, const std::string& content, bool packed) {
    std::ofstream f(path, std::ios::binary);
    if (!f) throw std::runtime_error("cannot write file");
    if (packed) {
        std::string c = compressScp(content);
        f.write(c.data(), (std::streamsize)c.size());
    } else {
        f.write(content.data(), (std::streamsize)content.size());
    }
}

std::string quoteString(const std::string& inner) {
    return "\"" + inner + "\"";
}

std::string stripQuotes(const std::string& value) {
    if (value.size() >= 2 && value.front() == '"' && value.back() == '"')
        return value.substr(1, value.size() - 2);
    return value;
}

std::string unquoteString(const std::string& value) {
    return stripQuotes(value);
}

std::string sniffScp(const std::string& content) {
    return looksPacked(content) ? "packed" : "text";
}

}
