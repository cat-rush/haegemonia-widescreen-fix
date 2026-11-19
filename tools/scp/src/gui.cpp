#include "gui.hpp"
#include "catalog.hpp"
#include "inflate.hpp"
#include <commctrl.h>
#include <commdlg.h>
#include <algorithm>
#include <functional>
#include <fstream>
#include <sstream>
#include <cwchar>
#include <cstring>

#pragma comment(lib, "comctl32.lib")
#pragma comment(lib, "comdlg32.lib")

namespace scp {

static App* g_app = nullptr;
static WNDPROC g_oldTreeProc = nullptr;
static const wchar_t* kMainClass = L"ScpEdMain";

enum {
    IDC_PALETTE = 100, IDC_TREE, IDC_STATUS,
    IDC_LBLNAME, IDC_NAMECOMBO, IDC_LBLLABEL, IDC_LABELEDIT, IDC_LBLLIFETIME, IDC_LIFETIMEEDIT,
    IDC_REMCHECK, IDC_LBLCOMMENT, IDC_COMMENTEDIT, IDC_LBLTYPE, IDC_TYPECOMBO,
    IDC_LBLVALUE, IDC_VALUEEDIT, IDC_LBLRETURNTYPE, IDC_RETURNTYPECOMBO, IDC_LBLEXPR, IDC_EXPREDIT,
    IDC_LBLPNAME, IDC_PNAMEEDIT,
    ID_TOOL_NEW = 200, ID_TOOL_OPEN, ID_TOOL_SAVE, ID_TOOL_PACK, ID_TOOL_DEL, ID_TOOL_UP, ID_TOOL_DN, ID_TOOL_DUP, ID_TOOL_REM, ID_TOOL_UNDO, ID_TOOL_REDO,
    IDM_NEW = 1000, IDM_OPEN, IDM_SAVE, IDM_SAVEPACKED, IDM_SAVEAS, IDM_EXIT,
    IDM_UNDO, IDM_REDO,
    IDM_INS_COMMAND, IDM_INS_GROUP, IDM_INS_PARAM, IDM_INS_CONDITIONS, IDM_INS_CONDITION,
    IDM_INS_SELECT, IDM_INS_SWITCH, IDM_INS_INCLUDE, IDM_INS_CALL, IDM_INS_RUN, IDM_INS_RETURN,
    IDM_INS_ONDEATH, IDM_INS_ONPROD, IDM_INS_ONDEST, IDM_INS_ONJUMPSTART, IDM_INS_ONJUMPEND, IDM_INS_ONJUMPSIG, IDM_INS_INITVARS,
    IDM_DEL, IDM_UP, IDM_DN, IDM_DUP, IDM_REM,
    IDM_EXPAND_ALL, IDM_COLLAPSE_ALL, IDM_EXPAND_CHILDREN, IDM_COLLAPSE_CHILDREN,
    IDM_ABOUT,
};

std::string App::w2u(const std::wstring& w) {
    if (w.empty()) return "";
    /* use wcslen: the caller's wstring may CONTAIN a trailing NUL
       (combo-box text read with an n+1 buffer) - converting w.size()
       bytes embedded the NUL into the output string, which then
       landed inside XML attribute values ("All\0") and broke the
       game's script parser (the e5m5 mission bug). */
    size_t len = wcslen(w.c_str());
    if (len == 0) return "";
    int n = WideCharToMultiByte(CP_UTF8, 0, w.c_str(), (int)len, nullptr, 0, nullptr, nullptr);
    std::string s(n, '\0');
    WideCharToMultiByte(CP_UTF8, 0, w.c_str(), (int)len, &s[0], n, nullptr, nullptr);
    return s;
}

std::wstring App::u2w(const std::string& s) {
    if (s.empty()) return L"";
    int n = MultiByteToWideChar(CP_UTF8, 0, s.c_str(), (int)s.size(), nullptr, 0);
    std::wstring w(n, L'\0');
    MultiByteToWideChar(CP_UTF8, 0, s.c_str(), (int)s.size(), &w[0], n);
    return w;
}

static void setW(HWND h, const std::string& s) {
    SetWindowTextW(h, App::u2w(s).c_str());
}
static std::string getW(HWND h) {
    int n = GetWindowTextLengthW(h);
    std::wstring w(n, L'\0');
    GetWindowTextW(h, &w[0], n + 1);
    return App::w2u(w);
}

static std::string nodeLabel(const Node* n) {
    std::string out;
    const std::string& t = n->tag;
    std::string lifetime = n->attr("Lifetime");
    std::string rem = n->attr("Rem");
    if (t == "Script") out = "Script";
    else if (t == "Commands") out = n->text.empty() ? "(Commands)" : n->text;
    else if (t == "Command") {
        out = n->text.empty() ? "(unnamed)" : n->text;
    } else if (t == "Parameters") out = "(Parameters)";
    else if (t == "Parameter") {
        std::string v = n->attr("Value");
        if (v.size() > 48) v = v.substr(0, 48) + "...";
        out = (n->text.empty() ? "(param)" : n->text) + "  =  " + v;
    } else if (t == "Conditions") out = "(Conditions)";
    else if (t == "Condition") {
        std::string e = n->text;
        if (e.size() > 60) e = e.substr(0, 60) + "...";
        out = (n->attr("ReturnType").empty() ? "" : n->attr("ReturnType") + ": ") + e;
    } else if (t == "Select") {
        std::string st;
        for (Node* c : n->children)
            if (c->tag == "Parameter" && c->text == "SelectType") st = stripQuotes(c->attr("Value"));
        out = st.empty() ? "(Select)" : "(Select: " + st + ")";
    } else if (t == "Switch") out = "(Switch)";
    else if (t == "Include") out = "Include: " + n->text;
    else if (t == "CallScript") out = "Call: " + n->text;
    else if (t == "RunScript") out = "Run: " + n->text;
    else if (t == "Return") out = "(Return)";
    else if (t == "StepThru") out = "(StepThru)";
    else if (t == "InitVariables") out = "(InitVariables)";
    else if (t.compare(0, 2, "On") == 0) out = t + (n->text.empty() ? "" : ": " + n->text);
    else out = t + (n->text.empty() ? "" : ": " + n->text);
    if (!lifetime.empty() && lifetime != "1") out += "  [L=" + lifetime + "]";
    if (rem == "1") out += "  (rem)";
    return out;
}

std::string App::labelOf(const Node* n) {
    return nodeLabel(n);
}

std::string App::getText(HWND h) {
    return getW(h);
}

void App::rebuildTree() {
    if (!hTree || !doc) return;
    updating = true;
    TreeView_DeleteAllItems(hTree);
    n2i.clear();
    i2n.clear();
    std::function<HTREEITEM(Node*, HTREEITEM)> add = [&](Node* n, HTREEITEM parent) -> HTREEITEM {
        std::wstring label = u2w(nodeLabel(n));
        TVINSERTSTRUCTW ins = {};
        ins.hParent = parent;
        ins.hInsertAfter = TVI_LAST;
        ins.item.mask = TVIF_TEXT | TVIF_PARAM;
        ins.item.pszText = &label[0];
        ins.item.lParam = (LPARAM)n;
        HTREEITEM h = TreeView_InsertItem(hTree, &ins);
        n2i[n] = h;
        i2n[h] = n;
        for (Node* c : n->children) add(c, h);
        return h;
    };
    add(doc, TVI_ROOT);
    if (sel && n2i.count(sel)) {
        TreeView_SelectItem(hTree, n2i[sel]);
        TreeView_EnsureVisible(hTree, n2i[sel]);
    }
    updating = false;
}

static void show(HWND h, bool on) {
    ShowWindow(h, on ? SW_SHOW : SW_HIDE);
}

void App::rebuildInspector() {
    updating = true;
    Node* n = sel;
    bool isCmd = n && n->tag == "Command";
    bool isGroup = n && n->tag == "Commands";
    bool isParam = n && n->tag == "Parameter";
    bool isCond = n && n->tag == "Condition";
    bool isStruct = n && (n->tag == "Include" || n->tag == "CallScript" || n->tag == "RunScript");
    bool isSelect = n && n->tag == "Select";
    bool isSwitch = n && n->tag == "Switch";
    bool isOnEvent = n && n->tag.compare(0, 2, "On") == 0;
    if (!n) {
        for (HWND h : {lblName, cboName, lblLabel, edLabel, lblLifetime, edLifetime, chkRem, lblComment, edComment,
                       lblType, cboType, lblValue, edValue, lblExpr, edExpr, lblPName, edPName, cboReturnType, lblReturnType}) show(h, false);
        updating = false;
        return;
    }
    show(lblComment, true); show(edComment, true);
    show(lblName, isCmd); show(cboName, isCmd);
    show(lblLabel, isGroup || isStruct || isOnEvent); show(edLabel, isGroup || isStruct || isOnEvent);
    show(lblLifetime, isCmd || isSelect || isSwitch || isGroup); show(edLifetime, isCmd || isSelect || isSwitch || isGroup);
    show(chkRem, isCmd || isGroup || isSelect || isSwitch);
    show(lblPName, isParam); show(edPName, isParam);
    show(lblType, isParam); show(cboType, isParam);
    show(lblValue, isParam); show(edValue, isParam);
    show(lblExpr, isCond); show(edExpr, isCond);
    show(lblReturnType, isCond); show(cboReturnType, isCond);
    if (isCmd) {
        setW(cboName, n->text);
        setW(edLifetime, n->attr("Lifetime"));
        SendMessageW(chkRem, BM_SETCHECK, n->attr("Rem") == "1" ? BST_CHECKED : BST_UNCHECKED, 0);
    }
    if (isGroup) {
        setW(edLabel, n->text);
        setW(edLifetime, n->attr("Lifetime"));
        SendMessageW(chkRem, BM_SETCHECK, n->attr("Rem") == "1" ? BST_CHECKED : BST_UNCHECKED, 0);
    }
    if (isSelect || isSwitch) {
        setW(edLifetime, n->attr("Lifetime"));
        SendMessageW(chkRem, BM_SETCHECK, n->attr("Rem") == "1" ? BST_CHECKED : BST_UNCHECKED, 0);
    }
    if (isStruct || isOnEvent) setW(edLabel, n->text);
    if (isParam) {
        setW(edPName, n->text);
        std::string type = n->attr("Type");
        static const char* types[] = {"", "Number", "Float", "Bool", "Logical", "String", "VEC3", "VEC4", "FRGB", "FRGBA", "ARGB", "color", "Array", "Anim", "All"};
        SendMessageW(cboType, CB_RESETCONTENT, 0, 0);
        for (const char* t : types) SendMessageW(cboType, CB_ADDSTRING, 0, (LPARAM)u2w(t).c_str());
        int idx = 0;
        for (int i = 0; i < 15; i++) if (type == types[i]) idx = i;
        SendMessageW(cboType, CB_SETCURSEL, idx, 0);
        if (type == "String") setW(edValue, stripQuotes(n->attr("Value")));
        else setW(edValue, n->attr("Value"));
    }
    if (isCond) {
        setW(edExpr, n->text);
        SendMessageW(cboReturnType, CB_RESETCONTENT, 0, 0);
        SendMessageW(cboReturnType, CB_ADDSTRING, 0, (LPARAM)L"Boolean");
        SendMessageW(cboReturnType, CB_ADDSTRING, 0, (LPARAM)L"Number");
        SendMessageW(cboReturnType, CB_SETCURSEL, n->attr("ReturnType") == "Number" ? 1 : 0, 0);
    }
    setW(edComment, n->attr("Comment"));
    updating = false;
}

void App::refreshAll() {
    rebuildTree();
    rebuildInspector();
}

void App::selectNode(Node* n) {
    if (!n) return;
    if (!updating) flushFields();
    sel = n;
    rebuildInspector();
    if (n2i.count(n)) {
        TreeView_SelectItem(hTree, n2i[n]);
        TreeView_EnsureVisible(hTree, n2i[n]);
    }
    if (hStatus) {
        std::string t = labelOf(n);
        if (t.size() > 120) t = t.substr(0, 120);
        SendMessageW(hStatus, SB_SETTEXTW, 1, (LPARAM)u2w(t).c_str());
    }
}

void App::setDirty() {
    dirty = true;
    updateTitle();
}

void App::updateTitle() {
    std::string t = filePath.empty() ? "Untitled" : filePath;
    t += dirty ? " *" : "";
    t += "  -  Haegemonia Script Editor (classic)";
    SetWindowTextW(hMain, u2w(t).c_str());
}

std::string App::serialize() {
    return doc ? serializeXml(doc) : "";
}

std::vector<int> App::selPath() const {
    std::vector<int> p;
    Node* n = sel;
    while (n && n->parent) { p.push_back((int)n->index()); n = n->parent; }
    std::reverse(p.begin(), p.end());
    return p;
}

Node* App::nodeFromPath(const std::vector<int>& p) {
    Node* n = doc;
    for (int i : p) {
        if (i < 0 || i >= (int)n->children.size()) return nullptr;
        n = n->children[i];
    }
    return n;
}

void App::pushUndo() {
    undo.push_back(serialize());
    if (undo.size() > 100) undo.erase(undo.begin());
    redo.clear();
}

void App::restoreFromString(const std::string& s, const std::vector<int>& selPath) {
    Node* old = doc;
    try {
        Node* parsed = parseXml(s);
        doc = parsed;
        delete old;
    } catch (std::exception& e) {
        MessageBoxA(hMain, e.what(), "Undo failed", MB_ICONERROR);
        return;
    }
    sel = nodeFromPath(selPath);
    refreshAll();
    dirty = true;
}

void App::doUndo(bool redoMode) {
    auto& from = redoMode ? redo : undo;
    auto& to = redoMode ? undo : redo;
    if (from.empty()) return;
    std::string s = from.back();
    std::vector<int> sp = selPath();
    from.pop_back();
    to.push_back(serialize());
    if (to.size() > 100) to.erase(to.begin());
    restoreFromString(s, sp);
}

void App::flushFields() {
    if (!sel || updating || !cboName) return;
    const std::string& t = sel->tag;
    if (t == "Command") {
        std::string name = getW(cboName);
        if (!name.empty() && name != sel->text) { pushUndo(); sel->text = name; }
        std::string lt = getW(edLifetime);
        if (lt != sel->attr("Lifetime")) { pushUndo(); if (lt.empty()) sel->delAttr("Lifetime"); else sel->setAttr("Lifetime", lt); }
        bool rem = SendMessageW(chkRem, BM_GETCHECK, 0, 0) == BST_CHECKED;
        if ((sel->attr("Rem") == "1") != rem) { pushUndo(); if (rem) sel->setAttr("Rem", "1"); else sel->delAttr("Rem"); }
    } else if (t == "Commands") {
        std::string lbl = getW(edLabel);
        if (lbl != sel->text) { pushUndo(); sel->text = lbl; }
        std::string lt = getW(edLifetime);
        if (lt != sel->attr("Lifetime")) { pushUndo(); if (lt.empty()) sel->delAttr("Lifetime"); else sel->setAttr("Lifetime", lt); }
        bool rem = SendMessageW(chkRem, BM_GETCHECK, 0, 0) == BST_CHECKED;
        if ((sel->attr("Rem") == "1") != rem) { pushUndo(); if (rem) sel->setAttr("Rem", "1"); else sel->delAttr("Rem"); }
    } else if (t == "Select" || t == "Switch") {
        std::string lt = getW(edLifetime);
        if (lt != sel->attr("Lifetime")) { pushUndo(); if (lt.empty()) sel->delAttr("Lifetime"); else sel->setAttr("Lifetime", lt); }
        bool rem = SendMessageW(chkRem, BM_GETCHECK, 0, 0) == BST_CHECKED;
        if ((sel->attr("Rem") == "1") != rem) { pushUndo(); if (rem) sel->setAttr("Rem", "1"); else sel->delAttr("Rem"); }
    } else if (t == "Include" || t == "CallScript" || t == "RunScript" || t.compare(0, 2, "On") == 0) {
        std::string v = getW(edLabel);
        if (v != sel->text) { pushUndo(); sel->text = v; }
    } else if (t == "Parameter") {
        std::string pn = getW(edPName);
        if (pn != sel->text) { pushUndo(); sel->text = pn; }
        std::wstring wt;
        int n = GetWindowTextLengthW(cboType);
        wt.resize(n + 1);
        GetWindowTextW(cboType, &wt[0], n + 1);
        std::string type = w2u(wt);
        if (type != sel->attr("Type")) { pushUndo(); if (type.empty()) sel->delAttr("Type"); else sel->setAttr("Type", type); }
        std::string v = getW(edValue);
        std::string val = (type == "String") ? quoteString(v) : v;
        if (val != sel->attr("Value")) { pushUndo(); sel->setAttr("Value", val); }
    } else if (t == "Condition") {
        std::string e = getW(edExpr);
        if (e != sel->text) { pushUndo(); sel->text = e; }
        std::string rt = SendMessageW(cboReturnType, CB_GETCURSEL, 0, 0) == 1 ? "Number" : "Boolean";
        if (rt != sel->attr("ReturnType")) { pushUndo(); sel->setAttr("ReturnType", rt); }
    }
    std::string c = getW(edComment);
    if (!sel->attr("Comment").empty() || !c.empty()) {
        if (c != sel->attr("Comment")) { pushUndo(); if (c.empty()) sel->delAttr("Comment"); else sel->setAttr("Comment", c); }
    }
}

static void addEditorAttrs(Node* n) {
    n->setAttr("ScriptEditorExpanded", "0");
    n->setAttr("ScriptEditorSelected", "0");
}

static bool isContainerTag(const std::string& t) {
    return t == "Commands" || t == "Select" || t == "Switch" || t == "InitVariables" ||
           t == "Script" || t.compare(0, 2, "On") == 0;
}

static void findContainer(Node* sel, Node* doc, Node*& container, size_t& idx) {
    Node* ref = sel;
    while (ref && ref->parent && !isContainerTag(ref->parent->tag)) ref = ref->parent;
    if (ref && ref->parent) {
        container = ref->parent;
        idx = ref->index() + 1;
    } else {
        container = doc;
        idx = doc->children.size();
    }
}

Node* App::insertNode(const std::string& tag, bool asChild) {
    if (!doc) return nullptr;
    Node* n = new Node(tag);
    addEditorAttrs(n);
    Node* parent = nullptr;
    size_t idx = 0;
    if (sel && (asChild || (isContainerTag(sel->tag) && sel != doc))) {
        parent = sel;
        idx = parent->children.size();
    } else {
        findContainer(sel, doc, parent, idx);
    }
    parent->insertChild(idx, n);
    sel = n;
    return n;
}

Node* App::insertCommand(const std::string& name) {
    flushFields();
    if (!doc) return nullptr;
    Node* parent = nullptr;
    size_t idx = 0;
    if (sel && isContainerTag(sel->tag) && sel != doc) {
        parent = sel;
        idx = parent->children.size();
    } else {
        findContainer(sel, doc, parent, idx);
    }
    return insertCommandAt(name, parent, idx);
}

Node* App::insertCommandAt(const std::string& name, Node* parent, size_t idx) {
    flushFields();
    if (!doc) return nullptr;
    if (!parent) parent = doc;
    if (!parent->parent && parent != doc) return nullptr;
    pushUndo();
    Node* n = new Node("Command");
    addEditorAttrs(n);
    n->text = name;
    n->setAttr("Lifetime", "1");
    Node* p = n->addChild("Parameters");
    addEditorAttrs(p);
    parent->insertChild(idx, n);
    sel = n;
    setDirty();
    refreshAll();
    return n;
}

bool App::moveNodeTo(Node* n, Node* parent, size_t idx) {
    if (!n || n == doc || !parent) return false;
    if (parent != doc && !parent->parent) return false;
    for (Node* a = parent; a; a = a->parent)
        if (a == n) return false;
    flushFields();
    pushUndo();
    if (n->parent == parent) {
        size_t i = n->index();
        if (idx > i) idx--;
    }
    n->detach();
    parent->insertChild(idx, n);
    sel = n;
    setDirty();
    refreshAll();
    return true;
}

Node* App::insertParameter() {
    flushFields();
    if (!doc || !sel) return nullptr;
    Node* cmd = nullptr;
    if (sel->tag == "Command" || sel->tag == "Select") cmd = sel;
    else if (sel->parent && (sel->parent->tag == "Command" || sel->parent->tag == "Select")) cmd = sel->parent;
    else if (sel->parent && sel->parent->tag == "Parameters" && sel->parent->parent) cmd = sel->parent->parent;
    if (!cmd) return nullptr;
    Node* params = nullptr;
    for (Node* c : cmd->children) if (c->tag == "Parameters") params = c;
    if (!params) { params = cmd->addChild("Parameters"); addEditorAttrs(params); }
    pushUndo();
    Node* n = params->addChild("Parameter");
    addEditorAttrs(n);
    n->text = "NewParameter";
    n->setAttr("Type", "All");
    n->setAttr("Value", "0");
    sel = n;
    setDirty();
    refreshAll();
    return n;
}

Node* App::insertCondition() {
    flushFields();
    if (!doc || !sel) return nullptr;
    pushUndo();
    Node* parent = nullptr;
    size_t idx = 0;
    if (isContainerTag(sel->tag) && sel != doc) {
        parent = sel;
        idx = 0;
    } else if (sel->tag == "Condition" && sel->parent) {
        parent = sel->parent;
        idx = sel->index() + 1;
    } else {
        findContainer(sel, doc, parent, idx);
        idx = 0;
    }
    Node* n = new Node("Condition");
    addEditorAttrs(n);
    n->setAttr("ReturnType", "Boolean");
    n->text = "true";
    parent->insertChild(idx, n);
    sel = n;
    setDirty();
    refreshAll();
    return n;
}

Node* App::insertStructural(const std::string& tag) {
    flushFields();
    if (!doc) return nullptr;
    pushUndo();
    Node* n = insertNode(tag, false);
    if (tag == "Commands") {
        n->text = "NEW GROUP";
    } else if (tag == "Conditions") {
        n->tag = "Commands";
        n->text = "CONDITIONS";
        Node* c = n->addChild("Condition");
        addEditorAttrs(c);
        c->setAttr("ReturnType", "Boolean");
        c->text = "true";
    } else if (tag == "Include" || tag == "CallScript" || tag == "RunScript") {
        n->text = "../randomevents/";
    } else if (tag == "Select") {
        n->setAttr("Lifetime", "0");
        Node* p = n->addChild("Parameters");
        addEditorAttrs(p);
        struct { const char* name; const char* value; } params[] = {
            {"EntityIDVariable", "\"SelectEntityID\""},
            {"EntityNrVariable", "\"SelectItemNr\""},
            {"SelectType", "\"Squad\""},
            {"SolarSystem", "$System"},
        };
        for (auto& pr : params) {
            Node* q = p->addChild("Parameter");
            addEditorAttrs(q);
            q->text = pr.name;
            q->setAttr("Type", "All");
            q->setAttr("Value", pr.value);
        }
    } else if (tag == "Switch") {
        n->setAttr("Lifetime", "0");
    } else if (tag == "OnDeath") {
        n->text = "\"entityid\"";
    } else if (tag == "InitVariables") {
        Node* g = n->addChild("Commands");
        addEditorAttrs(g);
        g->text = "VARIABLES";
    }
    setDirty();
    refreshAll();
    return n;
}

void App::deleteSel() {
    flushFields();
    if (!sel || sel == doc) return;
    if (!sel->children.empty()) {
        if (MessageBoxW(hMain, L"Delete this node and all its children?", L"Delete", MB_YESNO | MB_ICONQUESTION) != IDYES) return;
    }
    pushUndo();
    Node* parent = sel->parent;
    Node* victim = sel;
    size_t idx = victim->index();
    sel = nullptr;
    parent->removeChild(victim);
    if (!parent->children.empty()) sel = parent->children[std::min(idx, parent->children.size() - 1)];
    else sel = parent;
    setDirty();
    refreshAll();
}

void App::moveSel(int dir) {
    flushFields();
    if (!sel || !sel->parent) return;
    auto& v = sel->parent->children;
    size_t i = sel->index();
    size_t j = dir < 0 ? (i > 0 ? i - 1 : i) : (i + 1 < v.size() ? i + 1 : i);
    if (i == j) return;
    pushUndo();
    std::swap(v[i], v[j]);
    setDirty();
    refreshAll();
}

void App::duplicateSel() {
    flushFields();
    if (!sel || sel == doc) return;
    pushUndo();
    Node* c = sel->clone();
    sel->parent->insertChild(sel->index() + 1, c);
    sel = c;
    setDirty();
    refreshAll();
}

void App::toggleRem() {
    flushFields();
    if (!sel) return;
    pushUndo();
    if (sel->attr("Rem") == "1") sel->delAttr("Rem");
    else sel->setAttr("Rem", "1");
    setDirty();
    refreshAll();
}

void App::newDoc() {
    if (dirty) {
        int r = MessageBoxW(hMain, L"Discard unsaved changes?", L"New", MB_YESNO | MB_ICONQUESTION);
        if (r != IDYES) return;
    }
    if (doc) delete doc;
    doc = new Node("Script");
    Node* g = doc->addChild("Commands");
    addEditorAttrs(g);
    g->text = "INITIALIZATION";
    sel = g;
    filePath.clear();
    dirty = false;
    packed = false;
    undo.clear();
    redo.clear();
    refreshAll();
    updateTitle();
}

bool App::load(const std::string& path) {
    std::ifstream f(path, std::ios::binary);
    if (!f) {
        MessageBoxA(hMain, "cannot open file", "Load failed", MB_ICONERROR);
        return false;
    }
    std::stringstream ss;
    ss << f.rdbuf();
    std::string raw = ss.str();
    packed = looksPacked(raw);
    try {
        std::string content = packed ? decompressScp((const uint8_t*)raw.data(), raw.size()) : raw;
        Node* parsed = parseXml(content);
        if (doc) delete doc;
        doc = parsed;
        sel = doc;
        filePath = path;
        dirty = false;
        undo.clear();
        redo.clear();
        refreshAll();
        updateTitle();
        return true;
    } catch (std::exception& e) {
        MessageBoxA(hMain, e.what(), "Load failed", MB_ICONERROR);
        return false;
    }
}

bool App::save(const std::string& path, bool asPacked) {
    flushFields();
    try {
        saveScpFile(path, serialize(), asPacked);
        filePath = path;
        packed = asPacked;
        dirty = false;
        updateTitle();
        return true;
    } catch (std::exception& e) {
        MessageBoxA(hMain, e.what(), "Save failed", MB_ICONERROR);
        return false;
    }
}

static bool fileDlg(HWND h, bool save, std::string& path, bool& packed) {
    wchar_t buf[MAX_PATH] = L"";
    if (!path.empty()) wcscpy_s(buf, App::u2w(path).c_str());
    OPENFILENAMEW ofn = {};
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = h;
    ofn.lpstrFilter = L"Scripts (*.scp;*.scz;*.xml;*.inf)\0*.scp;*.scz;*.xml;*.inf\0All files\0*.*\0";
    ofn.lpstrFile = buf;
    ofn.nMaxFile = MAX_PATH;
    ofn.Flags = save ? OFN_OVERWRITEPROMPT : (OFN_FILEMUSTEXIST | OFN_HIDEREADONLY);
    BOOL ok = save ? GetSaveFileNameW(&ofn) : GetOpenFileNameW(&ofn);
    if (!ok) return false;
    path = App::w2u(buf);
    return true;
}

static void insCommit(App* a) {
    if (!a || a->updating) return;
    a->flushFields();
    a->refreshAll();
}

static LRESULT CALLBACK treeProc(HWND h, UINT m, WPARAM w, LPARAM l) {
    App* a = g_app;
    if (m == WM_KEYDOWN) {
        bool ctrl = (GetKeyState(VK_CONTROL) & 0x8000) != 0;
        bool alt = (GetKeyState(VK_MENU) & 0x8000) != 0;
        if (w == VK_DELETE) { a->deleteSel(); return 0; }
        if (ctrl && w == 'Z') { a->doUndo(false); return 0; }
        if (ctrl && w == 'Y') { a->doUndo(true); return 0; }
        if (ctrl && w == 'D') { a->duplicateSel(); return 0; }
        if (ctrl && w == 'R') { a->toggleRem(); return 0; }
        if (alt && w == VK_UP) { a->moveSel(-1); return 0; }
        if (alt && w == VK_DOWN) { a->moveSel(1); return 0; }
        if (w == VK_F2) {
            if (a->sel && a->sel->tag == "Command") { SetFocus(a->cboName); return 0; }
            if (a->sel && a->sel->tag == "Parameter") { SetFocus(a->edPName); return 0; }
            if (a->sel && a->sel->tag == "Condition") { SetFocus(a->edExpr); return 0; }
            if (a->sel && (a->sel->tag == "Include" || a->sel->tag == "CallScript" || a->sel->tag == "RunScript")) { SetFocus(a->edLabel); return 0; }
        }
    }
    return CallWindowProcW(g_oldTreeProc, h, m, w, l);
}

static void expandAllItems(HWND tree, HTREEITEM item) {
    TreeView_Expand(tree, item, TVE_EXPAND);
    for (HTREEITEM c = TreeView_GetChild(tree, item); c; c = TreeView_GetNextSibling(tree, c))
        expandAllItems(tree, c);
}

static void collapseAllItems(HWND tree, HTREEITEM item) {
    TreeView_Expand(tree, item, TVE_COLLAPSE);
}

static LRESULT CALLBACK mainProc(HWND h, UINT m, WPARAM w, LPARAM l) {
    App* a = g_app;
    switch (m) {
    case WM_CREATE: {
        INITCOMMONCONTROLSEX icc = {sizeof(icc), ICC_TREEVIEW_CLASSES | ICC_BAR_CLASSES | ICC_LISTVIEW_CLASSES};
        InitCommonControlsEx(&icc);
        a->hFont = CreateFontW(-12, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE, DEFAULT_CHARSET,
                               OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, CLEARTYPE_QUALITY, DEFAULT_PITCH, L"Segoe UI");
        a->hTool = CreateWindowExW(0, TOOLBARCLASSNAMEW, nullptr,
            WS_CHILD | WS_VISIBLE | TBSTYLE_FLAT | TBSTYLE_TOOLTIPS | CCS_NODIVIDER,
            0, 0, 0, 0, h, nullptr, a->inst, nullptr);
        TBADDBITMAP tb = {};
        SendMessageW(a->hTool, TB_ADDBITMAP, 0, (LPARAM)&tb);
        struct { int id; const wchar_t* txt; } btns[] = {
            {ID_TOOL_NEW, L"New"}, {ID_TOOL_OPEN, L"Open"}, {ID_TOOL_SAVE, L"Save"},
            {0, nullptr}, {ID_TOOL_UNDO, L"Undo"}, {ID_TOOL_REDO, L"Redo"}, {0, nullptr},
            {ID_TOOL_DEL, L"Del"}, {ID_TOOL_UP, L"Up"}, {ID_TOOL_DN, L"Dn"}, {ID_TOOL_DUP, L"Dup"}, {ID_TOOL_REM, L"Rem"},
        };
        for (auto& b : btns) {
            TBBUTTON tb2 = {};
            if (b.id == 0) { tb2.fsStyle = TBSTYLE_SEP; SendMessageW(a->hTool, TB_ADDBUTTONS, 1, (LPARAM)&tb2); continue; }
            std::wstring t = b.txt;
            tb2.iBitmap = I_IMAGENONE;
            tb2.idCommand = b.id;
            tb2.fsState = TBSTATE_ENABLED;
            tb2.fsStyle = BTNS_BUTTON | BTNS_AUTOSIZE;
            tb2.iString = (INT_PTR)t.c_str();
            SendMessageW(a->hTool, TB_ADDBUTTONS, 1, (LPARAM)&tb2);
        }
        a->hPalette = CreateWindowExW(0, WC_TREEVIEWW, L"",
            WS_CHILD | WS_VISIBLE | WS_BORDER | TVS_HASBUTTONS | TVS_LINESATROOT | TVS_SHOWSELALWAYS,
            0, 0, 0, 0, h, (HMENU)IDC_PALETTE, a->inst, nullptr);
        a->hTree = CreateWindowExW(0, WC_TREEVIEWW, L"",
            WS_CHILD | WS_VISIBLE | WS_BORDER | TVS_HASBUTTONS | TVS_HASLINES | TVS_LINESATROOT | TVS_SHOWSELALWAYS,
            0, 0, 0, 0, h, (HMENU)IDC_TREE, a->inst, nullptr);
        g_oldTreeProc = (WNDPROC)SetWindowLongPtrW(a->hTree, GWLP_WNDPROC, (LONG_PTR)treeProc);
        TreeView_SetBkColor(a->hTree, RGB(255, 255, 255));
        TreeView_SetBkColor(a->hPalette, RGB(250, 250, 250));
        {
            HWND p = CreateWindowExW(0, L"STATIC", L"", WS_CHILD | WS_VISIBLE | WS_CLIPSIBLINGS, 0, 0, 0, 0, h, nullptr, a->inst, nullptr);
            a->hInsGroup = p;
            auto mk = [&](const wchar_t* cls, const wchar_t* init, DWORD style, int id) -> HWND {
                return CreateWindowExW(0, cls, init, WS_CHILD | style, 0, 0, 0, 0, p, (HMENU)(INT_PTR)id, a->inst, nullptr);
            };
            auto lbl = [&](const wchar_t* t, int id) -> HWND { return mk(L"STATIC", t, WS_VISIBLE, id); };
            a->lblName = lbl(L"Command name", IDC_LBLNAME);
            a->cboName = mk(WC_COMBOBOXW, L"", WS_VISIBLE | CBS_DROPDOWN | CBS_AUTOHSCROLL | WS_VSCROLL | WS_TABSTOP, IDC_NAMECOMBO);
            a->lblLabel = lbl(L"Text / path", IDC_LBLLABEL);
            a->edLabel = mk(WC_EDITW, L"", WS_VISIBLE | ES_AUTOHSCROLL | WS_TABSTOP | WS_BORDER, IDC_LABELEDIT);
            a->lblLifetime = lbl(L"Lifetime (0 = loop)", IDC_LBLLIFETIME);
            a->edLifetime = mk(WC_EDITW, L"", WS_VISIBLE | ES_AUTOHSCROLL | WS_TABSTOP | WS_BORDER, IDC_LIFETIMEEDIT);
            a->chkRem = mk(L"BUTTON", L"Commented out (Rem)", WS_VISIBLE | BS_AUTOCHECKBOX | WS_TABSTOP, IDC_REMCHECK);
            a->lblComment = lbl(L"Comment", IDC_LBLCOMMENT);
            a->edComment = mk(WC_EDITW, L"", WS_VISIBLE | ES_AUTOHSCROLL | WS_TABSTOP | WS_BORDER, IDC_COMMENTEDIT);
            a->lblPName = lbl(L"Parameter name", IDC_LBLPNAME);
            a->edPName = mk(WC_EDITW, L"", WS_VISIBLE | ES_AUTOHSCROLL | WS_TABSTOP | WS_BORDER, IDC_PNAMEEDIT);
            a->lblType = lbl(L"Type", IDC_LBLTYPE);
            a->cboType = mk(WC_COMBOBOXW, L"", WS_VISIBLE | CBS_DROPDOWNLIST | WS_TABSTOP, IDC_TYPECOMBO);
            a->lblValue = lbl(L"Value", IDC_LBLVALUE);
            a->edValue = mk(WC_EDITW, L"", WS_VISIBLE | ES_AUTOHSCROLL | WS_TABSTOP | WS_BORDER, IDC_VALUEEDIT);
            a->lblExpr = lbl(L"Expression", IDC_LBLEXPR);
            a->edExpr = mk(WC_EDITW, L"", WS_VISIBLE | ES_MULTILINE | ES_WANTRETURN | WS_TABSTOP | WS_BORDER | WS_VSCROLL, IDC_EXPREDIT);
            a->lblReturnType = lbl(L"Returns", IDC_LBLRETURNTYPE);
            a->cboReturnType = mk(WC_COMBOBOXW, L"", WS_VISIBLE | CBS_DROPDOWNLIST | WS_TABSTOP, IDC_RETURNTYPECOMBO);
            for (HWND c : {a->lblName, a->cboName, a->lblLabel, a->edLabel, a->lblLifetime, a->edLifetime, a->chkRem,
                           a->lblPName, a->edPName, a->lblType, a->cboType, a->lblValue, a->edValue,
                           a->lblExpr, a->edExpr, a->lblReturnType, a->cboReturnType, a->lblComment, a->edComment}) {
                SendMessageW(c, WM_SETFONT, (WPARAM)a->hFont, TRUE);
            }
        }
        a->hStatus = CreateWindowExW(0, STATUSCLASSNAMEW, L"",
            WS_CHILD | WS_VISIBLE | SBARS_SIZEGRIP, 0, 0, 0, 0, h, (HMENU)IDC_STATUS, a->inst, nullptr);
        int widths[] = {300, 380, -1};
        SendMessageW(a->hStatus, SB_SETPARTS, 3, (LPARAM)widths);
        SendMessageW(a->hTool, TB_AUTOSIZE, 0, 0);
        {
            HMENU m = CreateMenu();
            HMENU f = CreatePopupMenu();
            AppendMenuW(f, MF_STRING, IDM_NEW, L"&New\tCtrl+N");
            AppendMenuW(f, MF_STRING, IDM_OPEN, L"&Open...\tCtrl+O");
            AppendMenuW(f, MF_SEPARATOR, 0, nullptr);
    AppendMenuW(f, MF_STRING, IDM_SAVE, L"&Save (packed)\tCtrl+S");
    AppendMenuW(f, MF_STRING, IDM_SAVEAS, L"Save &as...");
            AppendMenuW(f, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(f, MF_STRING, IDM_EXIT, L"E&xit");
            AppendMenuW(m, MF_POPUP, (UINT_PTR)f, L"&File");
            HMENU e = CreatePopupMenu();
            AppendMenuW(e, MF_STRING, IDM_UNDO, L"&Undo\tCtrl+Z");
            AppendMenuW(e, MF_STRING, IDM_REDO, L"&Redo\tCtrl+Y");
            AppendMenuW(e, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(e, MF_STRING, IDM_DEL, L"&Delete\tDel");
            AppendMenuW(e, MF_STRING, IDM_UP, L"Move &up\tAlt+Up");
            AppendMenuW(e, MF_STRING, IDM_DN, L"Move &down\tAlt+Down");
            AppendMenuW(e, MF_STRING, IDM_DUP, L"D&uplicate\tCtrl+D");
            AppendMenuW(e, MF_STRING, IDM_REM, L"Toggle &Rem\tCtrl+R");
            AppendMenuW(e, MF_SEPARATOR, 0, nullptr);
            HMENU ins = CreatePopupMenu();
            AppendMenuW(ins, MF_STRING, IDM_INS_COMMAND, L"&Command...");
            AppendMenuW(ins, MF_STRING, IDM_INS_GROUP, L"&Group (Commands)");
            AppendMenuW(ins, MF_STRING, IDM_INS_PARAM, L"&Parameter");
            AppendMenuW(ins, MF_STRING, IDM_INS_CONDITION, L"&Condition");
            AppendMenuW(ins, MF_STRING, IDM_INS_CONDITIONS, L"Condition &block");
            AppendMenuW(ins, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(ins, MF_STRING, IDM_INS_SELECT, L"&Select (iterate)");
            AppendMenuW(ins, MF_STRING, IDM_INS_SWITCH, L"S&witch");
            AppendMenuW(ins, MF_STRING, IDM_INS_INCLUDE, L"&Include");
            AppendMenuW(ins, MF_STRING, IDM_INS_CALL, L"&CallScript");
            AppendMenuW(ins, MF_STRING, IDM_INS_RUN, L"&RunScript");
            AppendMenuW(ins, MF_STRING, IDM_INS_RETURN, L"Re&turn");
            AppendMenuW(ins, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(ins, MF_STRING, IDM_INS_ONDEATH, L"OnDeat&h");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONPROD, L"OnProductionFinished");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONDEST, L"OnDestinationReached");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPSTART, L"OnJumpStart");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPEND, L"OnJumpEnd");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPSIG, L"OnJumpToSignaller");
            AppendMenuW(ins, MF_STRING, IDM_INS_INITVARS, L"Init&Variables");
            AppendMenuW(e, MF_POPUP, (UINT_PTR)ins, L"&Insert");
            AppendMenuW(m, MF_POPUP, (UINT_PTR)e, L"&Edit");
            HMENU hp = CreatePopupMenu();
            AppendMenuW(hp, MF_STRING, IDM_ABOUT, L"&About");
            AppendMenuW(m, MF_POPUP, (UINT_PTR)hp, L"&Help");
            SetMenu(h, m);
        }
        {
            ACCEL accs[] = {
                {FVIRTKEY | FCONTROL, 'N', IDM_NEW},
                {FVIRTKEY | FCONTROL, 'O', IDM_OPEN},
                {FVIRTKEY | FCONTROL, 'S', IDM_SAVE},
            };
            HACCEL ha = CreateAcceleratorTableW(accs, 3);
            SetPropW(h, L"accel", (HANDLE)ha);
        }
        {
            for (auto& cat : catalog()) {
                std::wstring cn = App::u2w(cat.name);
                TVINSERTSTRUCTW ins = {};
                ins.hParent = TVI_ROOT;
                ins.hInsertAfter = TVI_LAST;
                ins.item.mask = TVIF_TEXT;
                ins.item.pszText = &cn[0];
                HTREEITEM catItem = TreeView_InsertItem(a->hPalette, &ins);
                for (auto& c : cat.cmds) {
                    std::wstring cmd = App::u2w(c.name);
                    TVINSERTSTRUCTW ins2 = {};
                    ins2.hParent = catItem;
                    ins2.hInsertAfter = TVI_LAST;
                    ins2.item.mask = TVIF_TEXT;
                    ins2.item.pszText = &cmd[0];
                    TreeView_InsertItem(a->hPalette, &ins2);
                }
            }
        }
        return 0;
    }
    case WM_SIZE: {
        RECT rc;
        GetClientRect(h, &rc);
        int w = rc.right, ht = rc.bottom;
        SendMessageW(a->hTool, WM_SIZE, 0, 0);
        RECT tr;
        GetWindowRect(a->hTool, &tr);
        int toolH = tr.bottom - tr.top;
        int stH = 22;
        MoveWindow(a->hPalette, 0, toolH, 240, ht - toolH - stH, TRUE);
        MoveWindow(a->hTree, 240, toolH, w - 240 - 380, ht - toolH - stH, TRUE);
        MoveWindow(a->hInsGroup, w - 380, toolH, 380, ht - toolH - stH, TRUE);
        int y = 8, x = 10;
        auto row = [&](HWND l, HWND c, int ch) {
            MoveWindow(l, x, y + 3, 120, 20, TRUE);
            MoveWindow(c, x + 125, y, 245, ch, TRUE);
            y += ch + 6;
        };
        row(a->lblName, a->cboName, 24);
        row(a->lblLabel, a->edLabel, 22);
        row(a->lblLifetime, a->edLifetime, 22);
        y += 4;
        MoveWindow(a->chkRem, x, y, 240, 20, TRUE);
        y += 26;
        row(a->lblPName, a->edPName, 22);
        row(a->lblType, a->cboType, 24);
        row(a->lblValue, a->edValue, 22);
        y += 4;
        MoveWindow(a->lblExpr, x, y, 120, 20, TRUE);
        MoveWindow(a->edExpr, x, y + 18, 360, 110, TRUE);
        y += 132;
        row(a->lblReturnType, a->cboReturnType, 24);
        y += 4;
        row(a->lblComment, a->edComment, 22);
        SendMessageW(a->hStatus, WM_SIZE, 0, 0);
        return 0;
    }
    case WM_NOTIFY: {
        NMHDR* nm = (NMHDR*)l;
        if (nm->hwndFrom == a->hPalette && nm->code == NM_DBLCLK) {
            HTREEITEM hItem = TreeView_GetSelection(a->hPalette);
            TVITEMW item = {};
            item.hItem = hItem;
            item.mask = TVIF_TEXT | TVIF_PARAM;
            wchar_t buf[128];
            item.pszText = buf;
            item.cchTextMax = 128;
            TreeView_GetItem(a->hPalette, &item);
            HTREEITEM parent = TreeView_GetParent(a->hPalette, hItem);
            if (parent) {
                std::string name = a->w2u(buf);
                a->insertCommand(name);
            }
            return 0;
        }
        if (nm->hwndFrom == a->hTree && nm->code == TVN_SELCHANGED) {
            NMTREEVIEWW* tv = (NMTREEVIEWW*)nm;
            if (!a->updating) {
                a->flushFields();
                Node* n = a->i2n.count(tv->itemNew.hItem) ? a->i2n[tv->itemNew.hItem] : nullptr;
                if (n) {
                    a->sel = n;
                    a->rebuildInspector();
                    if (a->hStatus) {
                        std::string t = a->labelOf(n);
                        if (t.size() > 120) t = t.substr(0, 120);
                        SendMessageW(a->hStatus, SB_SETTEXTW, 1, (LPARAM)a->u2w(t).c_str());
                    }
                }
            }
            return 0;
        }
        return DefWindowProcW(h, m, w, l);
    }
    case WM_CONTEXTMENU: {
        if ((HWND)w == a->hTree) {
            POINT pt;
            GetCursorPos(&pt);
            TVHITTESTINFO ht = {};
            ht.pt = pt;
            ScreenToClient(a->hTree, &ht.pt);
            HTREEITEM hItem = TreeView_HitTest(a->hTree, &ht);
            if (!hItem) {
                HMENU menu = CreatePopupMenu();
                AppendMenuW(menu, MF_STRING, IDM_EXPAND_ALL, L"&Expand all");
                AppendMenuW(menu, MF_STRING, IDM_COLLAPSE_ALL, L"&Collapse all");
                TrackPopupMenu(menu, TPM_RIGHTBUTTON, pt.x, pt.y, 0, h, nullptr);
                DestroyMenu(menu);
                return 0;
            }
            TreeView_SelectItem(a->hTree, hItem);
            HMENU menu = CreatePopupMenu();
            HMENU ins = CreatePopupMenu();
            AppendMenuW(ins, MF_STRING, IDM_INS_COMMAND, L"&Command...");
            AppendMenuW(ins, MF_STRING, IDM_INS_GROUP, L"&Group (Commands)");
            AppendMenuW(ins, MF_STRING, IDM_INS_PARAM, L"&Parameter");
            AppendMenuW(ins, MF_STRING, IDM_INS_CONDITION, L"&Condition");
            AppendMenuW(ins, MF_STRING, IDM_INS_CONDITIONS, L"Condition &block");
            AppendMenuW(ins, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(ins, MF_STRING, IDM_INS_SELECT, L"&Select");
            AppendMenuW(ins, MF_STRING, IDM_INS_SWITCH, L"S&witch");
            AppendMenuW(ins, MF_STRING, IDM_INS_INCLUDE, L"&Include");
            AppendMenuW(ins, MF_STRING, IDM_INS_CALL, L"&CallScript");
            AppendMenuW(ins, MF_STRING, IDM_INS_RUN, L"&RunScript");
            AppendMenuW(ins, MF_STRING, IDM_INS_RETURN, L"Re&turn");
            AppendMenuW(ins, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(ins, MF_STRING, IDM_INS_ONDEATH, L"OnDeat&h");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONPROD, L"OnProductionFinished");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONDEST, L"OnDestinationReached");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPSTART, L"OnJumpStart");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPEND, L"OnJumpEnd");
            AppendMenuW(ins, MF_STRING, IDM_INS_ONJUMPSIG, L"OnJumpToSignaller");
            AppendMenuW(ins, MF_STRING, IDM_INS_INITVARS, L"Init&Variables");
            AppendMenuW(menu, MF_POPUP, (UINT_PTR)ins, L"Insert");
            AppendMenuW(menu, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(menu, MF_STRING, IDM_EXPAND_CHILDREN, L"&Expand subnodes");
            AppendMenuW(menu, MF_STRING, IDM_COLLAPSE_CHILDREN, L"&Collapse subnodes");
            AppendMenuW(menu, MF_SEPARATOR, 0, nullptr);
            AppendMenuW(menu, MF_STRING, IDM_DEL, L"&Delete\tDel");
            AppendMenuW(menu, MF_STRING, IDM_UP, L"Move &up");
            AppendMenuW(menu, MF_STRING, IDM_DN, L"Move &down");
            AppendMenuW(menu, MF_STRING, IDM_DUP, L"D&uplicate");
            AppendMenuW(menu, MF_STRING, IDM_REM, L"Toggle &Rem");
            TrackPopupMenu(menu, TPM_RIGHTBUTTON, pt.x, pt.y, 0, h, nullptr);
            DestroyMenu(ins);
            DestroyMenu(menu);
        }
        return 0;
    }
    case WM_COMMAND: {
        int id = LOWORD(w);
        int code = HIWORD(w);
        if (id >= IDC_NAMECOMBO && id <= IDC_EXPREDIT) {
            if (code == EN_KILLFOCUS || code == CBN_KILLFOCUS || code == CBN_SELCHANGE ||
                (id == IDC_REMCHECK && code == BN_CLICKED)) {
                PostMessageW(h, WM_APP + 1, 0, 0);
            }
        }
        switch (id) {
        case IDM_NEW: case ID_TOOL_NEW: a->newDoc(); break;
        case IDM_OPEN: case ID_TOOL_OPEN: {
            std::string p;
            bool pk = false;
            if (fileDlg(h, false, p, pk)) a->load(p);
            break;
        }
        case IDM_SAVE: case ID_TOOL_SAVE: {
            if (a->filePath.empty()) { std::string p; bool pk = false; if (fileDlg(h, true, p, pk)) a->save(p, true); }
            else a->save(a->filePath, true);
            break;
        }
        case IDM_SAVEPACKED: case ID_TOOL_PACK: {
            if (a->filePath.empty()) { std::string p; bool pk = false; if (fileDlg(h, true, p, pk)) a->save(p, true); }
            else a->save(a->filePath, true);
            break;
        }
        case IDM_SAVEAS: {
            std::string p = a->filePath;
            bool pk = a->packed;
            if (fileDlg(h, true, p, pk)) a->save(p, pk);
            break;
        }
        case IDM_EXIT: PostMessageW(h, WM_CLOSE, 0, 0); break;
        case IDM_UNDO: case ID_TOOL_UNDO: a->doUndo(false); break;
        case IDM_REDO: case ID_TOOL_REDO: a->doUndo(true); break;
        case IDM_INS_COMMAND: {
            POINT pt;
            GetCursorPos(&pt);
            HWND dlg = CreateWindowExW(0, L"#32770", L"Insert command", WS_CAPTION | WS_SYSMENU | WS_POPUP | DS_CENTER,
                                       pt.x - 100, pt.y - 50, 320, 160, h, nullptr, a->inst, nullptr);
            HWND cb = CreateWindowExW(0, WC_COMBOBOXW, L"", WS_CHILD | WS_VISIBLE | CBS_DROPDOWN | CBS_AUTOHSCROLL | WS_VSCROLL,
                                      10, 10, 280, 300, dlg, (HMENU)9001, a->inst, nullptr);
            std::vector<std::string> names;
            for (auto& cat : catalog())
                for (auto& c : cat.cmds) {
                    std::string n = std::string(cat.name) + "  |  " + c.name;
                    names.push_back(n);
                    SendMessageW(cb, CB_ADDSTRING, 0, (LPARAM)a->u2w(n).c_str());
                }
            SendMessageW(cb, CB_SETCURSEL, 0, 0);
            HWND ok = CreateWindowExW(0, L"BUTTON", L"OK", WS_CHILD | WS_VISIBLE | BS_DEFPUSHBUTTON, 120, 80, 80, 26, dlg, (HMENU)9002, a->inst, nullptr);
            HWND cancel = CreateWindowExW(0, L"BUTTON", L"Cancel", WS_CHILD | WS_VISIBLE, 210, 80, 80, 26, dlg, (HMENU)9003, a->inst, nullptr);
            ShowWindow(dlg, SW_SHOW);
            EnableWindow(h, FALSE);
            MSG msg;
            std::string chosen;
            while (GetMessageW(&msg, nullptr, 0, 0)) {
                if (msg.hwnd == dlg && msg.message == WM_COMMAND && LOWORD(msg.wParam) == 9002) {
                    int idx = SendMessageW(cb, CB_GETCURSEL, 0, 0);
                    if (idx >= 0 && idx < (int)names.size()) {
                        std::string n = names[idx];
                        size_t bar = n.find("  |  ");
                        chosen = n.substr(bar + 6);
                    }
                    break;
                }
                if (msg.hwnd == dlg && msg.message == WM_COMMAND && LOWORD(msg.wParam) == 9003) break;
                if (msg.hwnd == dlg && msg.message == WM_CLOSE) break;
                if (msg.message == WM_KEYDOWN && msg.wParam == VK_RETURN) {
                    int idx = SendMessageW(cb, CB_GETCURSEL, 0, 0);
                    if (idx >= 0 && idx < (int)names.size()) {
                        std::string n = names[idx];
                        size_t bar = n.find("  |  ");
                        chosen = n.substr(bar + 6);
                    }
                    break;
                }
                TranslateMessage(&msg);
                DispatchMessageW(&msg);
            }
            DestroyWindow(dlg);
            EnableWindow(h, TRUE);
            SetFocus(a->hTree);
            if (!chosen.empty()) a->insertCommand(chosen);
            break;
        }
        case IDM_INS_GROUP: a->insertStructural("Commands"); break;
        case IDM_INS_PARAM: a->insertParameter(); break;
        case IDM_INS_CONDITION: a->insertCondition(); break;
        case IDM_INS_CONDITIONS: a->insertStructural("Conditions"); break;
        case IDM_INS_SELECT: a->insertStructural("Select"); break;
        case IDM_INS_SWITCH: a->insertStructural("Switch"); break;
        case IDM_INS_INCLUDE: a->insertStructural("Include"); break;
        case IDM_INS_CALL: a->insertStructural("CallScript"); break;
        case IDM_INS_RUN: a->insertStructural("RunScript"); break;
        case IDM_INS_RETURN: a->insertStructural("Return"); break;
        case IDM_INS_ONDEATH: a->insertStructural("OnDeath"); break;
        case IDM_INS_ONPROD: a->insertStructural("OnProductionFinished"); break;
        case IDM_INS_ONDEST: a->insertStructural("OnDestinationReached"); break;
        case IDM_INS_ONJUMPSTART: a->insertStructural("OnJumpStart"); break;
        case IDM_INS_ONJUMPEND: a->insertStructural("OnJumpEnd"); break;
        case IDM_INS_ONJUMPSIG: a->insertStructural("OnJumpToSignaller"); break;
        case IDM_INS_INITVARS: a->insertStructural("InitVariables"); break;
        case IDM_DEL: case ID_TOOL_DEL: a->deleteSel(); break;
        case IDM_UP: case ID_TOOL_UP: a->moveSel(-1); break;
        case IDM_DN: case ID_TOOL_DN: a->moveSel(1); break;
        case IDM_DUP: case ID_TOOL_DUP: a->duplicateSel(); break;
        case IDM_REM: case ID_TOOL_REM: a->toggleRem(); break;
        case IDM_EXPAND_ALL: case IDM_COLLAPSE_ALL: {
            HTREEITEM root = TreeView_GetRoot(a->hTree);
            if (root) {
                if (id == IDM_EXPAND_ALL) expandAllItems(a->hTree, root);
                else collapseAllItems(a->hTree, root);
            }
            break;
        }
        case IDM_EXPAND_CHILDREN: case IDM_COLLAPSE_CHILDREN: {
            HTREEITEM item = TreeView_GetSelection(a->hTree);
            if (item) {
                if (id == IDM_EXPAND_CHILDREN) expandAllItems(a->hTree, item);
                else collapseAllItems(a->hTree, item);
            }
            break;
        }
        case IDM_ABOUT: MessageBoxW(h, L"Haegemonia Script Editor (classic)\n\nTree-view editor for .scp mission scripts.\nSupports packed (.scp/.scz) and text scripts.", L"About", MB_OK); break;
        }
        return 0;
    }
    case WM_APP + 1:
        insCommit(a);
        return 0;
    case WM_CLOSE: {
        if (a->dirty) {
            int r = MessageBoxW(h, L"Save changes before exit?", L"Exit", MB_YESNOCANCEL | MB_ICONQUESTION);
            if (r == IDCANCEL) return 0;
            if (r == IDYES) {
                if (a->filePath.empty()) {
                    std::string p;
                    bool pk = false;
                    if (fileDlg(h, true, p, pk)) a->save(p, true);
                    else return 0;
                } else {
                    a->save(a->filePath, true);
                }
            }
        }
        DestroyWindow(h);
        return 0;
    }
    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProcW(h, m, w, l);
}

App* createApp(HINSTANCE inst) {
    App* a = new App();
    a->inst = inst;
    g_app = a;
    WNDCLASSEXW wc = {};
    wc.cbSize = sizeof(wc);
    wc.lpfnWndProc = mainProc;
    wc.hInstance = inst;
    wc.hCursor = LoadCursor(nullptr, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
    wc.lpszClassName = kMainClass;
    RegisterClassExW(&wc);
    a->hMain = CreateWindowExW(0, kMainClass, L"Haegemonia Script Editor (classic)",
        WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN,
        CW_USEDEFAULT, CW_USEDEFAULT, 1280, 800, nullptr, nullptr, inst, nullptr);
    ShowWindow(a->hMain, SW_SHOW);
    UpdateWindow(a->hMain);
    return a;
}

int runApp(App* app) {
    HACCEL ha = (HACCEL)GetPropW(app->hMain, L"accel");
    MSG msg;
    while (GetMessageW(&msg, nullptr, 0, 0)) {
        if (ha && TranslateAcceleratorW(app->hMain, ha, &msg)) continue;
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }
    return (int)msg.wParam;
}

}
