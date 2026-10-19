import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHumanRow extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aihumanrow.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        w.println("==== XREFS TO FUN_007c99fe (human/static row appender?) ====");
        for (Reference r : getReferencesTo(toAddr(0x7c99feL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO FUN_007bf616 (start handler) ====");
        for (Reference r : getReferencesTo(toAddr(0x7bf616L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO FUN_007d3e99 ====");
        for (Reference r : getReferencesTo(toAddr(0x7d3e99L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");

        long[] dec = { 0x7d3e99L, 0x56c3afL };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
