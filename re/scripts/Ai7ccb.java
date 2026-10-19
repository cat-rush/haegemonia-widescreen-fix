import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai7ccb extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai7ccb_report.txt"));
        long[] sites = {0x007ccbd5L, 0x007c3b7aL, 0x007c4304L, 0x007cc939L};
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long t : sites) {
            Function f = getFunctionContaining(toAddr(t));
            w.println("=== site 0x" + Long.toHexString(t) + " in " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none") + " ===");
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 180, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
