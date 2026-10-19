import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HeadProjFns extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("headprojfns_report.txt"));
        long[] addrs = { 0x4a3289L, 0x4a32e1L, 0x4a3500L, 0x4e85c5L, 0x4e8e0fL, 0x4e8eb8L, 0x4e84edL, 0x4e83b8L };
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
