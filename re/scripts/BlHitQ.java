import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitQ extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitq_report.txt"));
        w.println("==== refs to FUN_0079d304 (click processor callers) ====");
        Reference[] refs = getReferencesTo(toAddr(0x0079d304L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        w.println("==== refs to FUN_007a03b4 ====");
        refs = getReferencesTo(toAddr(0x007a03b4L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x007a03b4L));
        if (f == null) f = getFunctionContaining(toAddr(0x007a03b4L));
        w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
        DecompileResults r = di.decompileFunction(f, 90, monitor);
        if (r != null && r.decompileCompleted()) {
            String c = r.getDecompiledFunction().getC();
            if (c.length() > 70000) c = c.substring(0, 70000) + "\n... TRUNCATED ...";
            w.println(c);
        } else {
            w.println("DECOMP FAILED");
        }
        w.close();
        println("done");
    }
}
