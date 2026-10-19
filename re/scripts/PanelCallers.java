import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PanelCallers extends GhidraScript {
    void dec(DecompInterface d, long va, PrintWriter w, String tag) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) {
            w.println("== " + tag + " 0x" + Long.toHexString(va) + ": NO FUNCTION ==");
            return;
        }
        w.println("==== " + tag + " " + f.getName() + " (" + f.getEntryPoint() + " .. " +
                  f.getBody().getMaxAddress() + ") ====");
        DecompileResults r = d.decompileFunction(f, 120, monitor);
        if (r != null && r.decompileCompleted()) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("  (decompile failed)");
        }
    }
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\panelcallers_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long t = 0x006c4b8fL;
        w.println("=== xrefs (callers) to 0x6c4b8f");
        Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
        if (it != null) {
            while (it.hasNext()) {
                Reference r = it.next();
                Function f = getFunctionContaining(r.getFromAddress());
                w.println("   from " + r.getFromAddress() + (f != null ? " " + f.getName() + " " + f.getBody() : " ?"));
            }
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        /* likely callers: panel update fn */
        dec(d, 0x006cc5b5L, w, "fn after 6c4b8f (panel update?)");
        w.close();
        d.dispose();
        println("done");
    }
}
