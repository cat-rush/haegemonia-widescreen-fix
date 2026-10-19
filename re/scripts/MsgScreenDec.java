import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgScreenDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("msgscreendec_report.txt"));
        ghidra.program.model.listing.Function f = getFunctionAt(toAddr(0x76bc4aL));
        if (f == null) { w.println("no fn"); w.close(); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults r = di.decompileFunction(f, 120, monitor);
        if (r != null && r.decompileCompleted()) {
            String c = r.getDecompiledFunction().getC();
            int idx = c.indexOf("FUN_00788e0a");
            if (idx < 0) idx = c.indexOf("788e0a");
            if (idx < 0) { w.println("call not found in decompile"); w.println(c.substring(0, 4000)); }
            else {
                w.println("=== context around FUN_00788e0a call ===");
                w.println(c.substring(Math.max(0, idx - 2500), Math.min(c.length(), idx + 2500)));
            }
        } else {
            w.println("decompile failed: " + (r != null ? r.getErrorMessage() : "null"));
        }
        w.close();
    }
}
