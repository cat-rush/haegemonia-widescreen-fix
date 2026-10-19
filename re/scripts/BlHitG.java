import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitG extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitg_report.txt"));
        w.println("==== refs to FUN_0077efc8 (all types) ====");
        Reference[] refs = getReferencesTo(toAddr(0x0077efc8L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x0078da5fL, 0x00692ef4L};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 40000) c = c.substring(0, 40000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
        }
        w.close();
        println("done");
    }
}
