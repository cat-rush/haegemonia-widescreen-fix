import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.ConsoleTaskMonitor;
import java.io.FileWriter;
import java.io.PrintWriter;

/* Decompile the layout-constant users:
   FUN_00812e1d (3x layoutW reads), FUN_009390e4 + FUN_0093b4bf
   (layoutCenter readers) - candidates for the panel container layout. */
public class LayoutDecomp extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\layoutdecomp_report.txt"));
        long[] fns = {0x00812e1dL, 0x009390e4L, 0x0093b4bfL};
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
