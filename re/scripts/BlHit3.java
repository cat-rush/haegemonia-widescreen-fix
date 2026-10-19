import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit3_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x007a16b4L, 0x007a34e0L, 0x007a37f6L, 0x0078dc2eL, 0x0078da5fL};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 50000) c = c.substring(0, 50000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
            w.println("---- callers of " + f.getName() + " ----");
            Reference[] refs = getReferencesTo(f.getEntryPoint());
            for (Reference ref : refs) {
                if (ref.getReferenceType().isCall()) {
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    String fname = cf == null ? "?" : cf.getName();
                    w.println("   from " + ref.getFromAddress() + " in " + fname);
                }
            }
        }
        w.println("==== callers of FUN_00799b3e ====");
        Reference[] refs = getReferencesTo(toAddr(0x00799b3eL));
        for (Reference ref : refs) {
            if (ref.getReferenceType().isCall()) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName();
                w.println("   from " + ref.getFromAddress() + " in " + fname);
            }
        }
        w.println("==== callers of FUN_0077efc8 ====");
        refs = getReferencesTo(toAddr(0x0077efc8L));
        for (Reference ref : refs) {
            if (ref.getReferenceType().isCall()) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName();
                w.println("   from " + ref.getFromAddress() + " in " + fname);
            }
        }
        w.close();
        println("done");
    }
}
