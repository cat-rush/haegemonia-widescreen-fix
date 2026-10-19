import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

public class Ai7dXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai7dxref_report.txt"));
        long[] targets = {0x007d2d10L, 0x007d2e1aL, 0x007d2f84L, 0x007d1ef2L};
        for (long t : targets) {
            w.println("==== xrefs to 0x" + Long.toHexString(t) + " ====");
            Reference[] refs = getReferencesTo(toAddr(t));
            Set<Function> fns = new LinkedHashSet<Function>();
            for (Reference r : refs) {
                Address from = r.getFromAddress();
                Function f = getFunctionContaining(from);
                w.println("  from " + from + (f != null ? " in " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
                if (f != null) fns.add(f);
            }
            for (Function f : fns) {
                w.println("");
                w.println("========== DECOMP " + f.getName() + " @ " + f.getEntryPoint() + " ==========");
                DecompInterface di = new DecompInterface();
                di.openProgram(currentProgram);
                DecompileResults res = di.decompileFunction(f, 180, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
        }
        w.close();
        println("done");
    }
}
