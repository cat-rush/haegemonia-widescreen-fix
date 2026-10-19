import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ModelCull2 extends GhidraScript {
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
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\modelcull2_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        dec(d, 0x004ed349L, w, "model visibility check?");
        dec(d, 0x004eb538L, w, "model draw");
        w.close();
        d.dispose();
        println("done");
    }
}
