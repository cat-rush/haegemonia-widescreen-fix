#include "gui.hpp"
#include <commctrl.h>

int WINAPI wWinMain(HINSTANCE inst, HINSTANCE, PWSTR cmdLine, int) {
    scp::App* app = scp::createApp(inst);
    if (!app) return 1;
    std::wstring cl = cmdLine ? cmdLine : L"";
    if (!cl.empty() && cl.front() == '"') {
        size_t e = cl.find('"', 1);
        cl = e == std::wstring::npos ? cl.substr(1) : cl.substr(1, e - 1);
    }
    if (!cl.empty()) {
        app->load(app->w2u(cl));
    } else {
        app->newDoc();
    }
    int r = scp::runApp(app);
    delete app;
    return r;
}
