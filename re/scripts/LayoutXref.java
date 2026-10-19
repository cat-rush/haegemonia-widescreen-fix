import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LayoutXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\layoutxref_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] targets = { 0x00b3a848L, 0x00b316d4L };
        for (long t : targets) {
            w.println("==== xrefs to " + Long.toHexString(t) + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(t));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                long from = ref.getFromAddress().getOffset();
                if (from < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("    " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " .. " + cf.getBody().getMaxAddress() : ""));
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
