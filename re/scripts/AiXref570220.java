import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiXref570220 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aixref570220_report.txt"));
        long[] addrs = { 0x570220L, 0x56e89aL };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("==== xrefs to 0x" + Long.toHexString(a) + " (" + (f != null ? f.getName() : "?") + ") ====");
            if (f != null) {
                for (Reference r : getReferencesTo(f.getEntryPoint())) {
                    Function caller = getFunctionContaining(r.getFromAddress());
                    w.println("  from " + r.getFromAddress() + " in " + (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
