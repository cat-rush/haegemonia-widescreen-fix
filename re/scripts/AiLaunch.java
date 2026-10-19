import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiLaunch extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ailaunch.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] dec = { 0x538461L, 0x539483L };
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
        w.println("==== XREFS TO 0x539483 ====");
        for (Reference r : getReferencesTo(toAddr(0x539483L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.close();
        println("done");
    }
}
