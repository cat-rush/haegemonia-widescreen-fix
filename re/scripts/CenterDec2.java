import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CenterDec2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\centerdec2_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] chain = { 0x009371aaL, 0x009390e4L };
        for (long t : chain) {
            Function f = getFunctionAt(toAddr(t));
            w.println("==== callers of " + Long.toHexString(t) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : "") + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(t));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " .. " + cf.getBody().getMaxAddress() : ""));
            }
        }
        Function f = getFunctionAt(toAddr(0x009390e4L));
        if (f != null) {
            w.println("==== FUN_009390e4 body ====");
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
