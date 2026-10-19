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

public class AiTsvCallers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aitsv_report.txt"));
        w.println("==== xrefs to FUN_0045c8b3 (TSV filler) ====");
        Reference[] refs = getReferencesTo(toAddr(0x0045c8b3L));
        Set<Function> fns = new LinkedHashSet<Function>();
        for (Reference r : refs) {
            Address from = r.getFromAddress();
            Function f = getFunctionContaining(from);
            w.println("  from " + from + (f != null ? " in " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            if (f != null) fns.add(f);
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (Function f : fns) {
            w.println("");
            w.println("========== DECOMP " + f.getName() + " @ " + f.getEntryPoint() + " ==========");
            DecompileResults res = di.decompileFunction(f, 120, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
        }
        w.close();
        println("done");
    }
}
