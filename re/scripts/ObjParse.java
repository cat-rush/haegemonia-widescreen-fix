import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ObjParse extends GhidraScript {
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
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\objparse_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        dec(d, 0x0048fbdaL, w, "OBST chunk parser (0x70)");
        dec(d, 0x0048fd55L, w, "OBSK chunk parser (0x7C)");
        dec(d, 0x0048ecebL, w, "chunk 0x6C parser");
        w.close();
        d.dispose();
        println("done");
    }
}
