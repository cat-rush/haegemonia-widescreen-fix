import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RowDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(f != null ? f.getEntryPoint().getOffset() : va) + " ----");
        if (f != null) w.println("range: " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rowdump_report.txt"));
        dec(0x004f21c2L);   /* row height fn */
        dec(0x0053c520L);   /* row widget setup */
        dec(0x004ff9a0L);   /* text area reset (10000.0f arg) */
        w.close();
        println("done");
    }
}
