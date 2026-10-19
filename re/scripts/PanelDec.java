import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PanelDec extends GhidraScript {
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
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\paneldec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        w.println("== functions in 0x6c3000..0x6ca200 ==");
        for (Function f : currentProgram.getFunctionManager().getFunctions(toAddr(0x6c3000L), true)) {
            if (f.getEntryPoint().getOffset() > 0x6ca200L) break;
            w.println("   " + f.getName() + " @ " + f.getEntryPoint() + " body " + f.getBody());
        }
        dec(d, 0x006c3b4dL, w, "strip renderer");
        dec(d, 0x006c4a10L, w, "panel 6c4a10");
        dec(d, 0x006c9e00L, w, "near 6c9ec2");
        dec(d, 0x006c9ec2L, w, "at 6c9ec2");
        w.close();
        d.dispose();
        println("done");
    }
}
