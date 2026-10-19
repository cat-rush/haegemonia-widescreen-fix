import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TrackXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\trackxref_report.txt"));
        long[] addrs = { 0x0050D7A5L, 0x0050D7CFL, 0x005471B5L };
        for (long a : addrs) {
            w.println("refs to 0x" + Long.toHexString(a) + ":");
            for (Reference r : getReferencesTo(toAddr(a))) {
                Address from = r.getFromAddress();
                Function cf = getFunctionContaining(from);
                w.println("   from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
            }
        }
        w.close();
        println("done");
    }
}
