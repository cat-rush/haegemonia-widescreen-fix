import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.ConsoleTaskMonitor;
import java.io.FileWriter;
import java.io.PrintWriter;

/* Decompile the known panel renderers and dump them for the layout hunt:
   FUN_0077efc8 (BL squad panel), FUN_0077ae14 (TL empire top bar). */
public class PanelDecomp extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\paneldecomp_report.txt"));
        long[] fns = {0x0077efc8L, 0x0077ae14L};
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
