import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PanelUpd extends GhidraScript {
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
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\panelupd_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        dec(d, 0x006cc5d5L, w, "panel update? (6cc5d5)");
        dec(d, 0x006cd8e1L, w, "panel 6cd8e1");
        dec(d, 0x006cd979L, w, "panel 6cd979");
        w.close();
        d.dispose();
        println("done");
    }
}
