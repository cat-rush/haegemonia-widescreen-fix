// ScreenDump.java - decompile + string refs for the frame functions
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.AddressSetView;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ScreenDump extends GhidraScript {
    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 300, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) {
            w.println("DECOMPILE FAILED (null)");
            w.flush();
            di.dispose();
            return;
        }
        String c = dr.getDecompiledFunction().getC();
        if (c.length() > 14000) c = c.substring(0, 14000);
        w.println(c);
        w.flush();
        di.dispose();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\screen_report.txt"));
        dec(0x00690c9bL);
        dec(0x0069e24eL);
        w.close();
        println("done: screen_report.txt");
    }
}
