import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiAddR1Callers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aiaddr1callers.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] targets = { 0x828312L, 0x828663L, 0x828b49L };
        for (long t : targets) {
            w.println("==== XREFS TO 0x" + Long.toHexString(t) + " ====");
            for (Reference r : getReferencesTo(toAddr(t))) {
                Function caller = getFunctionContaining(r.getFromAddress());
                w.println("  from " + r.getFromAddress() + " in " +
                    (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
            }
            w.println("");
        }
        w.println("==== XREFS TO 0x570db0 (row assign) ====");
        for (Reference r : getReferencesTo(toAddr(0x570db0L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO 0x5707d0 (serializer) ====");
        for (Reference r : getReferencesTo(toAddr(0x5707d0L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO 0x570220 (deserializer) ====");
        for (Reference r : getReferencesTo(toAddr(0x570220L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO popup AddRow 0x45e350 ====");
        for (Reference r : getReferencesTo(toAddr(0x45e350L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");
        w.println("==== XREFS TO 0x45d02c-area fill fns (popup row filler) ====");
        for (Reference r : getReferencesTo(toAddr(0x45d000L))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.close();
        println("done");
    }
}
