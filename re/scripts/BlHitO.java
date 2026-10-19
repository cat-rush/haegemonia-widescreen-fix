import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitO extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhito_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x007133fcL, 0x0071347eL, 0x007135e0L};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 30000) c = c.substring(0, 30000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
            w.println("---- callers ----");
            Reference[] refs = getReferencesTo(f.getEntryPoint());
            for (Reference ref : refs) {
                if (ref.getReferenceType().isCall()) {
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    String fname = cf == null ? "?" : cf.getName();
                    w.println("   from " + ref.getFromAddress() + " in " + fname);
                }
            }
        }
        w.close();
        println("done");
    }
}
