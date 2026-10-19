import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SceneHandlers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("scenehandlers_report.txt"));
        long[] addrs = { 0x7e84eeL, 0x7e862eL, 0x7e9839L, 0x7e8d65L, 0x7e8d25L, 0x7e47d9L };
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long a : addrs) {
            ghidra.program.model.listing.Function f = getFunctionAt(toAddr(a));
            if (f == null) { w.println("=== NO FN at " + Long.toHexString(a)); continue; }
            w.println("=== DECOMP " + Long.toHexString(a) + " (" + f.getName() + " "
                + Long.toHexString(f.getEntryPoint().getOffset()) + ".."
                + Long.toHexString(f.getBody().getMaxAddress().getOffset()) + ")");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed: " + (r != null ? r.getErrorMessage() : "null"));
            }
            w.println();
        }
        w.close();
    }
}
