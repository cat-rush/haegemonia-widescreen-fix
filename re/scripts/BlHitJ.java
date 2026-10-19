import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitJ extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitj_report.txt"));
        w.println("==== xrefs to c68038 (click btn) from 0x760000..0x7B0000 ====");
        Reference[] refs = getReferencesTo(toAddr(0x00c68038L));
        for (Reference ref : refs) {
            long fa = ref.getFromAddress().getOffset();
            if (fa < 0x760000L || fa > 0x7B0000L) continue;
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName() + " [" + cf.getEntryPoint() + ".." + cf.getBody().getMaxAddress() + "]";
            w.println("   from " + ref.getFromAddress() + " in " + fname);
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x004a0119L};
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
        }
        w.close();
        println("done");
    }
}
