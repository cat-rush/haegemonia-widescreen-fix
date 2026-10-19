import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PickDec2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pickdec2_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x005357a9L, 0x0053582dL, 0x00535955L,
                         0x006cd979L, 0x006cc5d5L, 0x0054f002L, 0x006941a7L };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f != null) {
                w.println("---- " + f.getName() + " @ " + Long.toHexString(a) + " .. " + f.getBody().getMaxAddress() + " ----");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                }
                ghidra.program.model.symbol.Reference[] refs =
                    getReferencesTo(toAddr(a));
                w.println("  callers:");
                for (ghidra.program.model.symbol.Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
                }
            } else {
                w.println("---- no function at " + Long.toHexString(a));
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
