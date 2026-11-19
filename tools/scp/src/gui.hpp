#pragma once
#include <windows.h>
#include <commctrl.h>
#include <string>
#include <vector>
#include <map>
#include "model.hpp"

namespace scp {

struct BlocksView;
struct PaletteView;

struct App {
    HINSTANCE inst = nullptr;
    HWND hMain = nullptr;
    HWND hTool = nullptr;
    HWND hCanvas = nullptr;
    HWND hSearch = nullptr;
    HWND hPalette = nullptr;
    HWND hTree = nullptr;
    HWND hStatus = nullptr;
    HWND hInsGroup = nullptr;
    HWND cboName = nullptr, edLabel = nullptr, edLifetime = nullptr, chkRem = nullptr,
         edComment = nullptr, cboType = nullptr, edValue = nullptr, cboReturnType = nullptr,
         edExpr = nullptr, edPName = nullptr;
    HWND lblName = nullptr, lblLabel = nullptr, lblLifetime = nullptr, lblComment = nullptr,
         lblType = nullptr, lblValue = nullptr, lblExpr = nullptr, lblPName = nullptr,
         lblReturnType = nullptr;
    HFONT hFont = nullptr;

    BlocksView* blocks = nullptr;
    PaletteView* palette = nullptr;
    bool blocksMode = true;

    Node* doc = nullptr;
    Node* sel = nullptr;
    std::string filePath;
    bool dirty = false;
    bool packed = false;
    bool updating = false;
    std::vector<std::string> undo;
    std::vector<std::string> redo;
    std::map<Node*, HTREEITEM> n2i;
    std::map<HTREEITEM, Node*> i2n;

    ~App() { if (doc) delete doc; }

    void newDoc();
    bool load(const std::string& path);
    bool save(const std::string& path, bool asPacked);
    void rebuildTree();
    void rebuildInspector();
    void refreshAll();
    void selectNode(Node* n);
    void pushUndo();
    void doUndo(bool redoMode);
    Node* insertNode(const std::string& tag, bool asChild);
    Node* insertCommand(const std::string& name);
    Node* insertCommandAt(const std::string& name, Node* parent, size_t idx);
    bool moveNodeTo(Node* n, Node* parent, size_t idx);
    Node* insertParameter();
    Node* insertCondition();
    Node* insertStructural(const std::string& tag);
    void deleteSel();
    void moveSel(int dir);
    void duplicateSel();
    void toggleRem();
    void flushFields();
    void setDirty();
    void updateTitle();
    std::string serialize();
    void restoreFromString(const std::string& s, const std::vector<int>& selPath);
    std::vector<int> selPath() const;
    Node* nodeFromPath(const std::vector<int>& p);
    static std::string w2u(const std::wstring& w);
    static std::wstring u2w(const std::string& s);
    static std::string wc(const wchar_t* w) { return w2u(w); }
    static std::string labelOf(const Node* n);
    static std::string getText(HWND h);
};

App* createApp(HINSTANCE inst);
int runApp(App* app);

}
