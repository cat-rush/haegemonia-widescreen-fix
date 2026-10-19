// BoxArea.java - decompile the ship-list box-scene chain + screen-area helpers
// for the "detect the whole-screen 4:3 area" task. Report -> boxarea_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = {
            0x00473078L, 0x0047523aL, 0x0047352fL, 0x004aaa32L,
            0x004bbf63L, 0x004ba82bL, 0x004ba962L,
            0x0071a9e0L, 0x0071aab0L, 0x0071ab8cL, 0x0071ab42L,
            0x0044864eL
        };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); continue; }
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
            w.println("callers:");
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
            else w.println("(decompile failed)");
        }
        d.dispose();
        w.close();
        println("done: boxarea_report.txt");
    }
}
