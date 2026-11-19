#include "gui.hpp"
#include <cstdio>
#include <cstring>

static int fails = 0;
#define CHECK(cond, msg) do { if (!(cond)) { printf("FAIL: %s\n", msg); fails++; } else { printf("ok:   %s\n", msg); } } while (0)

static int countTag(scp::Node* n, const char* t) {
    int c = 0;
    for (auto* k : n->children) if (k->tag == t) c++;
    return c;
}

int main() {
    scp::App app;
    app.inst = GetModuleHandleW(nullptr);
    app.newDoc();
    CHECK(app.doc && app.doc->tag == "Script", "newDoc creates Script root");

    scp::Node* grp = app.doc->children[0];
    CHECK(grp->tag == "Commands" && grp->text == "INITIALIZATION", "newDoc creates INITIALIZATION group");

    app.sel = grp;
    scp::Node* cmd = app.insertCommand("StartTimer");
    CHECK(cmd && cmd->text == "StartTimer" && cmd->parent == grp, "insertCommand inside group");
    CHECK(cmd->attr("Lifetime") == "1" && countTag(cmd, "Parameters") == 1, "command Lifetime=1 + Parameters");

    app.sel = cmd;
    scp::Node* p = app.insertParameter();
    CHECK(p && p->parent == cmd->children[0], "insertParameter goes into Parameters");

    app.sel = p;
    scp::Node* sel = app.insertStructural("Select");
    CHECK(sel && sel->tag == "Select" && sel->parent == grp, "Select inserted as sibling of the command (not inside Parameters)");
    CHECK(sel->attr("Lifetime") == "0" && countTag(sel, "Parameters") == 1, "Select Lifetime=0 with Parameters");
    CHECK(sel->children[0]->children.size() == 4, "Select has 4 default filter params");

    app.sel = sel;
    scp::Node* condGrp = app.insertStructural("Commands");
    CHECK(condGrp && condGrp->parent == sel, "group inserted inside selected container (Select)");
    condGrp->text = "IF TEST";
    app.sel = condGrp;
    scp::Node* cond = app.insertCondition();
    CHECK(cond && cond->tag == "Condition" && cond->parent == condGrp, "condition is direct child of group (game format)");
    CHECK(condGrp->children[0] == cond, "condition is first child of group");

    scp::Node* cond2 = app.insertCondition();
    CHECK(cond2->parent == condGrp && condGrp->children[1] == cond2, "second condition is sibling (AND)");

    app.sel = condGrp;
    scp::Node* win = app.insertCommand("Win");
    CHECK(win->parent == condGrp, "Win appended into selected group");
    size_t cgBefore = condGrp->children.size();
    CHECK(app.moveNodeTo(win, app.doc, 0), "moveNodeTo root-level");
    CHECK(app.doc->children[0] == win && condGrp->children.size() == cgBefore - 1, "moved between parents");

    CHECK(!app.moveNodeTo(win, win, 0), "cannot move node into itself");
    CHECK(!app.moveNodeTo(grp, cmd, 0), "cannot move into descendant");

    app.sel = cmd;
    size_t gBefore = grp->children.size();
    app.duplicateSel();
    CHECK(grp->children.size() == gBefore + 1 && grp->children[1] == app.sel && app.sel->text == "StartTimer", "duplicate adds sibling after original");

    app.sel = cmd;
    app.toggleRem();
    CHECK(cmd->attr("Rem") == "1", "toggleRem sets Rem=1");
    app.toggleRem();
    CHECK(cmd->attr("Rem") == "", "toggleRem clears Rem");

    CHECK(condGrp->index() == 1, "condGrp is after Parameters inside Select");
    app.sel = condGrp;
    app.moveSel(-1);
    CHECK(condGrp->index() == 0 && sel->children[0] == condGrp, "moveSel up reorders");

    std::string s1 = app.serialize();
    size_t szBefore = grp->children.size();
    app.sel = grp->children[2];
    app.deleteSel();
    CHECK(grp->children.size() == szBefore - 1, "deleteSel removes node");
    app.doUndo(false);
    CHECK(app.serialize() == s1, "undo restores document");
    app.doUndo(true);
    grp = app.doc->children[1];
    CHECK(grp->children.size() == szBefore - 1, "redo reapplies delete");

    std::string text = app.serialize();
    scp::saveScpFile("C:\\Users\\Nova\\AppData\\Local\\Temp\\opencode\\classic_text.scp", text, false);
    scp::saveScpFile("C:\\Users\\Nova\\AppData\\Local\\Temp\\opencode\\classic_packed.scp", text, true);

    scp::App app2;
    app2.inst = app.inst;
    CHECK(app2.load("C:\\Users\\Nova\\AppData\\Local\\Temp\\opencode\\classic_text.scp") && app2.serialize() == text, "text save/load roundtrip");
    CHECK(app2.load("C:\\Users\\Nova\\AppData\\Local\\Temp\\opencode\\classic_packed.scp") && app2.serialize() == text, "packed save/load roundtrip");
    CHECK(app2.packed, "packed flag detected");

    printf(fails ? "FAILURES: %d\n" : "ALL OK\n", fails);
    return fails;
}
