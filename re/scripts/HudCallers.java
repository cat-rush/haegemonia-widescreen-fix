import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HudCallers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\hudcallers_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] targets = { 0x0076bc4aL, 0x007a8730L };
        for (long t : targets) {
            Function f = getFunctionAt(toAddr(t));
            w.println("==== callers of " + Long.toHexString(t) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : "") + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(t));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " .. " + cf.getBody().getMaxAddress() : ""));
            }
            if (t == 0x007a8730L && f != null) {
                w.println("==== FUN_007a8730 body ====");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
