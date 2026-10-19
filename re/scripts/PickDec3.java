import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PickDec3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pickdec3_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x007b957aL, 0x0071ca2aL, 0x0072194bL, 0x00741c9fL,
                         0x0076a6e4L, 0x0077a24bL, 0x0070d0c1L, 0x00761742L,
                         0x00756c70L, 0x007e176dL, 0x0053975aL };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            w.println("---- " + Long.toHexString(a) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : " (no fn)") + " ----");
            if (f != null) {
                ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(a));
                for (ghidra.program.model.symbol.Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("    caller " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
