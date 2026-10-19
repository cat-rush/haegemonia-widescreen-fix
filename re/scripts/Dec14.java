import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec14 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec14_report.txt"));
        long[] addrs = {0x00c0a790L, 0x00c0a794L};
        for (long a : addrs) {
            w.println("======== xrefs to 0x" + Long.toHexString(a));
            int n = 0;
            for (Reference ref : getReferencesTo(toAddr(a))) {
                Function f = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + "  in " +
                          (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
                if (++n >= 40) { w.println("  ..."); break; }
            }
            if (n == 0) w.println("  none");
        }
        w.close();
        println("done");
    }
}
