import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiLaunch3Dec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ailaunch3dec.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] addrs = { 0x00667637L, 0x006676efL, 0x00664e87L, 0x00666839L, 0x00939090L, 0x00938fc8L, 0x00662028L };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("===== containing 0x" + Long.toHexString(a) + " =====");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println();
        }
        w.close();
        println("done");
    }
}
