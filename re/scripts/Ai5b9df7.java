import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai5b9df7 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai5b9df7_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] addrs = { 0x5b9df7L, 0x5bc927L };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 180, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
                w.println("== xrefs:");
                for (Reference r : getReferencesTo(f.getEntryPoint())) {
                    Function caller = getFunctionContaining(r.getFromAddress());
                    w.println("  from " + r.getFromAddress() + " in " + (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
