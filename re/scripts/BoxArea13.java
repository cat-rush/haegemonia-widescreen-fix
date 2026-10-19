// BoxArea13.java - decompile the center-attribute value source
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea13 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea13_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x0040b738L, 0x00434a40L };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); continue; }
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
            else w.println("(decompile failed)");
        }
        d.dispose();
        w.close();
        println("done: boxarea13_report.txt");
    }
}
