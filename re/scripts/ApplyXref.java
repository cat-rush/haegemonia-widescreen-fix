import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ApplyXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\applyxref_report.txt"));
        long[] addrs = { 0x007336A6L, 0x0073373EL, 0x007342A2L, 0x00733606L };
        for (long a : addrs) {
            w.println("refs to 0x" + Long.toHexString(a) + ":");
            int n = 0;
            for (Reference r : getReferencesTo(toAddr(a))) {
                Address from = r.getFromAddress();
                Function cf = getFunctionContaining(from);
                w.println("   from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
                if (++n > 40) { w.println("   ..."); break; }
            }
        }
        w.close();
        println("done");
    }
}
