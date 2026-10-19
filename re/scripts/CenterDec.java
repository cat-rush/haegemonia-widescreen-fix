import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CenterDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\centerdec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x006c3b4dL, 0x009390e4L, 0x0093b4bfL };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==== " + Long.toHexString(a) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : " (no fn)") + " ====");
            if (f != null) {
                ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(a));
                w.println("  callers:");
                for (ghidra.program.model.symbol.Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " .. " + cf.getBody().getMaxAddress() : ""));
                }
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                } else {
                    w.println("(decompile failed)");
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
