#pragma once
#include <cstdint>
#include <string>
#include <vector>
#include <functional>
#include <algorithm>
#include <stdexcept>

namespace scp {

struct Node {
    std::string tag;
    std::vector<std::pair<std::string, std::string>> attrs;
    std::string text;
    std::vector<Node*> children;
    Node* parent = nullptr;

    Node(std::string t, Node* p = nullptr) : tag(std::move(t)), parent(p) {}
    ~Node() { for (Node* c : children) delete c; }

    std::string attr(const std::string& name) const {
        for (auto& a : attrs) if (a.first == name) return a.second;
        return "";
    }
    void setAttr(const std::string& name, const std::string& value) {
        for (auto& a : attrs) if (a.first == name) { a.second = value; return; }
        attrs.emplace_back(name, value);
    }
    void delAttr(const std::string& name) {
        for (size_t i = 0; i < attrs.size(); i++)
            if (attrs[i].first == name) { attrs.erase(attrs.begin() + i); return; }
    }
    Node* addChild(const std::string& tag) {
        Node* c = new Node(tag, this);
        children.push_back(c);
        return c;
    }
    size_t index() const {
        if (!parent) return 0;
        for (size_t i = 0; i < parent->children.size(); i++)
            if (parent->children[i] == this) return i;
        return 0;
    }
    void detach() {
        if (!parent) return;
        auto& v = parent->children;
        v.erase(std::remove(v.begin(), v.end(), this), v.end());
        parent = nullptr;
    }
    void insertChild(size_t idx, Node* c) {
        c->parent = this;
        if (idx > children.size()) idx = children.size();
        children.insert(children.begin() + idx, c);
    }
    void removeChild(Node* c) {
        auto it = std::find(children.begin(), children.end(), c);
        if (it != children.end()) children.erase(it);
        delete c;
    }
    Node* clone(Node* newParent = nullptr) const {
        Node* n = new Node(tag, newParent);
        n->attrs = attrs;
        n->text = text;
        for (Node* c : children) n->children.push_back(c->clone(n));
        return n;
    }
    Node* root() {
        Node* n = this;
        while (n->parent) n = n->parent;
        return n;
    }
    bool isCmd() const { return tag == "Command" || tag == "Select" || tag == "Switch"; }
    bool hasLifetime() const {
        return tag == "Command" || tag == "Select" || tag == "Switch" ||
               tag == "RunScript" || tag == "OnProductionFinished" || tag == "Commands";
    }
};

std::string xmlEscape(const std::string& s);
std::string xmlUnescape(const std::string& s);
Node* parseXml(const std::string& text);
std::string serializeXml(Node* root);
std::string loadScpFile(const std::string& path);
std::string sniffScp(const std::string& content);
void saveScpFile(const std::string& path, const std::string& content, bool packed);
bool looksPacked(const std::string& content);
std::string quoteString(const std::string& inner);
std::string unquoteString(const std::string& value);
std::string stripQuotes(const std::string& value);

}
