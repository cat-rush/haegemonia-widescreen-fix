// Fn234e.java - decompile FUN_004f234e (label text measurement)
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Fn234e extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fn234e_report.txt"));
        Function f = getFunctionAt(toAddr(0x004f234eL));
        if (f == null) { w.println("NO FUNCTION at 4f234e"); w.close(); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        w.println(dr.getDecompiledFunction().getC());
        di.dispose();
        w.close();
        println("done: fn234e_report.txt");
    }
}
