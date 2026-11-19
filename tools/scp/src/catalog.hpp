#pragma once
#include <string>
#include <vector>

namespace scp {

struct CmdInfo {
    const char* name;
    const char* params;
    const char* help;
};

struct CmdCat {
    const char* name;
    std::vector<CmdInfo> cmds;
};

const std::vector<CmdCat>& catalog();

}
