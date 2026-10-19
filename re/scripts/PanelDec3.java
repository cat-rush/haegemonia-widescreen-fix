import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PanelDec3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\paneldec3_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x006c4b8fL));
        if (f != null) {
            w.println("==== FUN_006c4b8f (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("  (decompile failed)");
            }
        } else {
            w.println("NO FUNCTION at 6c4b8f");
        }
        w.close();
        d.dispose();
        println("done");
    }
}
