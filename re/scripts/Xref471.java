import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Xref471 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xref471_report.txt"));
        long[] targets = {0x004471c6L, 0x004471f6L, 0x0044728eL, 0x00447337L, 0x004e85c5L};
        for (long t : targets) {
            w.println("==== xrefs to " + Long.toHexString(t) + " ====");
            Reference[] refs = getReferencesTo(toAddr(t));
            for (Reference r : refs) {
                Address from = r.getFromAddress();
                Function f = getFunctionContaining(from);
                w.println("  from " + from + (f != null ? " in " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            }
        }
        w.close();
        println("done");
    }
}
