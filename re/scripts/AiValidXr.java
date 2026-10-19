import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiValidXr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aivalidxr.txt"));
        long[] t = { 0x81f880L, 0x8203b8L, 0x7a9540L };
        for (long a : t) {
            w.println("==== XREFS TO 0x" + Long.toHexString(a) + " ====");
            for (Reference r : getReferencesTo(toAddr(a))) {
                Function caller = getFunctionContaining(r.getFromAddress());
                w.println("  from " + r.getFromAddress() + " in " +
                    (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
