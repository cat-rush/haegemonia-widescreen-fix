import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit7 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit7_report.txt"));
        w.println("==== xrefs to c68274 ====");
        Reference[] refs = getReferencesTo(toAddr(0x00c68274L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName() + " [" + cf.getEntryPoint() + ".." + cf.getBody().getMaxAddress() + "]";
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x007a16b4L, 0x008075d8L, 0x0078bd79L};
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
            w.println("---- callers ----");
            Reference[] r2 = getReferencesTo(f.getEntryPoint());
            for (Reference ref : r2) {
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
