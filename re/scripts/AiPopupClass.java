import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

public class AiPopupClass extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aipopcls_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionContaining(toAddr(0x00525e9eL));
        w.println("==== popup renderer 525e9e ====");
        if (f != null) {
            DecompileResults res = di.decompileFunction(f, 90, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("failed");
            w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
        }
        w.println("");
        w.println("==== callers of 525e9e ====");
        Reference[] refs = getReferencesTo(toAddr(0x00525e9eL));
        Set<Function> fset = new LinkedHashSet<Function>();
        for (Reference r : refs) {
            Address from = r.getFromAddress();
            Function cf = getFunctionContaining(from);
            w.println("  from " + from + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + ")" : ""));
            if (cf != null) fset.add(cf);
        }
        for (Function cf : fset) {
            w.println("");
            w.println("========== DECOMP caller " + cf.getName() + " @ " + cf.getEntryPoint() + " ==========");
            DecompileResults res = di.decompileFunction(cf, 90, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
        }
        w.close();
        println("done");
    }
}
