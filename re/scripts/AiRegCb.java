import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiRegCb extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\airegcb.txt"));
        w.println("==== XREFS TO FUN_0056c8fb (registry slot registration) ====");
        for (Reference r : getReferencesTo(toAddr(0x56c8fbL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO FUN_0055561b (registry lookup) ====");
        for (Reference r : getReferencesTo(toAddr(0x55561bL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO FUN_007bf616 ====");
        for (Reference r : getReferencesTo(toAddr(0x7bf616L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.close();
        println("done");
    }
}
