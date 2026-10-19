import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MboxDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mboxdec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x0048be3dL, 0x0048c5caL };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null)
                    w.println(r.getDecompiledFunction().getC());
                else w.println("(decompile failed)");
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
