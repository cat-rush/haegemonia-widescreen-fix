import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.ConsoleTaskMonitor;
import java.io.FileWriter;
import java.io.PrintWriter;

/* FUN_004f31b0: the attribute/layout handler called by the parser with
   the anchor strings - find the anchor vocabulary + the centering math. */
public class AnchorDecomp extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\anchordecomp_report.txt"));
        long[] fns = {0x004f31b0L};
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
        }
        w.close();
        println("done");
    }
}
