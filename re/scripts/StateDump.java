// StateDump.java - screen state getter + picker context
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class StateDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) {
            w.println("DECOMPILE FAILED (null)");
            w.flush(); di.dispose(); return;
        }
        String c = dr.getDecompiledFunction().getC();
        if (c.length() > 12000) c = c.substring(0, 12000);
        w.println(c);
        w.flush(); di.dispose();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\state_report.txt"));
        dec(0x0068c68eL);   // screen state getter
        dec(0x0054f002L);   // pickA caller (strip context)
        dec(0x005356e8L);   // pickB caller
        w.close();
        println("done: state_report.txt");
    }
}
