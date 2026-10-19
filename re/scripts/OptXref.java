import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class OptXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\optxref_report.txt"));
        long[] addrs = { 0xC21548L, 0xC2153CL, 0xC21544L };
        for (long a : addrs) {
            w.println("refs to 0x" + Long.toHexString(a) + ":");
            int n = 0;
            for (Reference r : getReferencesTo(toAddr(a))) {
                Address from = r.getFromAddress();
                Function cf = getFunctionContaining(from);
                w.println("   from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
                if (++n > 20) { w.println("   ..."); break; }
            }
        }
        w.close();
        println("done");
    }
}
