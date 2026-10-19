// TextSys.java - decompile the text system (FUN_004f0a57) + funnel (FUN_004ef060)
// to find the vertical centering/positioning formula used by label containers.
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TextSys extends GhidraScript {
    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 300, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) {
            w.println("DECOMPILE FAILED");
        } else {
            w.println(dr.getDecompiledFunction().getC());
        }
        w.flush();
        di.dispose();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\textsys_report.txt"));
        dec(0x004f0a57L);
        dec(0x004ef060L);
        w.close();
        println("done: textsys_report.txt");
    }
}
