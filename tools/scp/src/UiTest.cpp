#include <windows.h>
#include <commctrl.h>
#include <cstdio>
#include <cstring>

static HWND findNth(HWND parent, const wchar_t* cls, int& n) {
    HWND h = GetWindow(parent, GW_CHILD);
    while (h) {
        wchar_t c[256] = {0};
        GetClassNameW(h, c, 256);
        if (!wcscmp(c, cls)) {
            if (n == 0) return h;
            n--;
        }
        HWND r = findNth(h, cls, n);
        if (r) return r;
        h = GetWindow(h, GW_HWNDNEXT);
    }
    return nullptr;
}

static HWND findChildNth(HWND parent, const wchar_t* cls, int nth) {
    int n = nth;
    return findNth(parent, cls, n);
}

static int countVisible(HWND tree) {
    int n = 0;
    HTREEITEM h = TreeView_GetRoot(tree);
    while (h) {
        n++;
        h = TreeView_GetNextItem(tree, h, TVGN_NEXTVISIBLE);
    }
    return n;
}

int wmain(int argc, wchar_t** argv) {
    DWORD pid = _wtoi(argv[1]);
    HWND main = nullptr;
    HWND h = GetTopWindow(nullptr);
    while (h) {
        DWORD p = 0;
        GetWindowThreadProcessId(h, &p);
        wchar_t cls[256] = {0};
        GetClassNameW(h, cls, 256);
        if (p == pid && !wcscmp(cls, L"ScpEdMain")) main = h;
        h = GetWindow(h, GW_HWNDNEXT);
    }
    if (!main) { printf("main not found\n"); return 1; }
    HWND tree = findChildNth(main, L"SysTreeView32", 1);
    if (!tree) { printf("tree not found\n"); return 1; }
    HTREEITEM root = TreeView_GetRoot(tree);
    if (!root) { printf("no root\n"); return 1; }

    printf("initial visible items: %d\n", countVisible(tree));
    SendMessageW(main, WM_COMMAND, 1032, 0); // IDM_COLLAPSE_ALL
    Sleep(200);
    int collapsed = countVisible(tree);
    printf("after collapse all: %d visible\n", collapsed);
    SendMessageW(main, WM_COMMAND, 1031, 0); // IDM_EXPAND_ALL
    Sleep(200);
    int expanded = countVisible(tree);
    printf("after expand all: %d visible\n", expanded);
    TreeView_SelectItem(tree, root);
    SendMessageW(main, WM_COMMAND, 1034, 0); // IDM_COLLAPSE_CHILDREN
    Sleep(200);
    int collapsedRoot = countVisible(tree);
    printf("after collapse subnodes: %d visible\n", collapsedRoot);
    SendMessageW(main, WM_COMMAND, 1033, 0); // IDM_EXPAND_CHILDREN
    Sleep(200);
    int expandedRoot = countVisible(tree);
    printf("after expand subnodes: %d visible\n", expandedRoot);

    bool ok = collapsed == 1 && expanded > 50 && collapsedRoot == 1 && expandedRoot == expanded;
    printf(ok ? "EXPAND/COLLAPSE OK\n" : "EXPAND/COLLAPSE FAIL\n");
    return ok ? 0 : 1;
}
