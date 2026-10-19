import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ResXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\resxref_report.txt"));
        long[] addrs = { 0xC295D8L, 0xC295F0L, 0xC29668L, 0xC2975CL };
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
