import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.ConsoleTaskMonitor;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/* FUN_006c3b4d = the strip renderer with "layoutCenter-0.5*w centering"
   (per MODLOG). Decompile it + list its callers to see whether the
   corner panels go through it. */
public class StripDecomp extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\stripdecomp_report.txt"));
        DecompInterface dec = new DecompInterface();
        dec.openProgram(currentProgram);
        ConsoleTaskMonitor mon = new ConsoleTaskMonitor();
        long f = 0x006c3b4dL;
        w.println("===== FUN_006c3b4d =====");
        DecompileResults r = dec.decompileFunction(getFunctionAt(toAddr(f)), 120, mon);
        if (r != null && r.decompileCompleted()) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("decompile failed: " + (r != null ? r.getErrorMessage() : "null"));
        }
        w.println();
        w.println("===== callers of FUN_006c3b4d =====");
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(f));
        if (it != null) {
            int c = 0;
            while (it.hasNext() && c < 30) {
                Reference rr = it.next();
                w.println("   from " + rr.getFromAddress() + " type=" + rr.getReferenceType()
                          + (getFunctionContaining(rr.getFromAddress()) != null ? " fn=" + getFunctionContaining(rr.getFromAddress()).getName() : ""));
                c++;
            }
        }
        w.close();
        println("done");
    }
}
