import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SquadDec2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\squaddec2_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] targets = { 0x0077efc8L, 0x0054f002L };
        for (long t : targets) {
            w.println("==== callers of " + Long.toHexString(t) + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(t));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " .. " + cf.getBody().getMaxAddress() : ""));
            }
        }
        long[] addrs = { 0x00780c83L };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==== " + Long.toHexString(a) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : " (no fn)") + " ====");
            if (f != null) {
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
