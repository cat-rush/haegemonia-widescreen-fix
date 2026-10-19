import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiXrefBefbe extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aixrefbefbe_report.txt"));
        long[] addrs = { 0x7befbeL, 0x7bfaf9L, 0x7cb3f4L };
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
