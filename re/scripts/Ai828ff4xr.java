import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai828ff4xr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai828ff4xr.txt"));
        w.println("==== XREFS TO 0x828ff4 ====");
        for (Reference r : getReferencesTo(toAddr(0x828ff4L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.close();
        println("done");
    }
}
