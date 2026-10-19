import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ResXref2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\resxref2_report.txt"));
        long[] addrs = { 0xC21605L, 0xC21611L, 0xC21625L, 0xC21631L, 0xC06818L };
        for (long a : addrs) {
            w.println("refs to 0x" + Long.toHexString(a) + ":");
            int n = 0;
            for (Reference r : getReferencesTo(toAddr(a))) {
                Address from = r.getFromAddress();
                Function cf = getFunctionContaining(from);
                w.println("   from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
                if (++n > 30) { w.println("   ..."); break; }
            }
        }
        w.close();
        println("done");
    }
}
