import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHdrDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aihdrdec.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] addrs = { 0x007d4e10L, 0x007d4e30L };
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
