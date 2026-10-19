import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HeadQuadChain extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("headquadchain_report.txt"));
        long[] addrs = { 0x4753d4L, 0x4732e5L };
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long a : addrs) {
            ghidra.program.model.listing.Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("=== NO FN containing " + Long.toHexString(a)); continue; }
            w.println("=== DECOMP " + f.getName() + " " +
                Long.toHexString(f.getEntryPoint().getOffset()) + ".." +
                Long.toHexString(f.getBody().getMaxAddress().getOffset()) +
                " (target " + Long.toHexString(a) + ")");
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
