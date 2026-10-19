import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ctor513b extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ctor513b_report.txt"));
        long[] sites = {0x00513bd5L, 0x005142d5L, 0x006eae69L, 0x00510ff4L};
        for (long s : sites) {
            Function f = getFunctionContaining(toAddr(s));
            w.println("== site " + Long.toHexString(s) + " in " +
                      (f != null ? f.getName() + " [" + f.getEntryPoint() + ".." + f.getBody().getMaxAddress() + "]" : "NO FN"));
            if (f != null) {
                Reference[] refs = getReferencesTo(f.getEntryPoint());
                for (Reference r : refs) {
                    if (r.getReferenceType().isCall()) {
                        w.println("   caller from " + r.getFromAddress());
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
