import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.ConsoleTaskMonitor;
import java.io.FileWriter;
import java.io.PrintWriter;

/* Decompile:
   - FUN_004f3512: the attribute parser that strcmp's "center"
   - FUN_00719f0a: the in-game HUD builder that emits "#fjob##w800##acenter#"
   - FUN_0077ae14 / FUN_0077efc8 already known (TL/BL panel renderers)
   to find how the panel x is computed and where the centering happens. */
public class CenterDecomp extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\centerdecomp_report.txt"));
        long[] fns = {0x004f3512L, 0x00719f0aL};
        DecompInterface dec = new DecompInterface();
        dec.openProgram(currentProgram);
        ConsoleTaskMonitor mon = new ConsoleTaskMonitor();
        for (long f : fns) {
            w.println("===== FUN_" + Long.toHexString(f) + " =====");
            DecompileResults r = dec.decompileFunction(getFunctionAt(toAddr(f)), 120, mon);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed: " + (r != null ? r.getErrorMessage() : "null"));
            }
            w.println();
        }
        w.close();
        println("done");
    }
}
