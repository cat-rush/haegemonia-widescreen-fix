import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnRange3 extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnrange3_report.txt"));
        Function f = getFunctionAt(toAddr(0x0050f465L));
        if (f == null) f = getFunctionContaining(toAddr(0x0050f465L));
        if (f != null) {
            w.println("FUN_0050f465 -> " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompInterface di = new DecompInterface();
            di.openProgram(currentProgram);
            DecompileResults dr = di.decompileFunction(f, 120, monitor);
            if (dr != null && dr.getDecompiledFunction() != null) {
                w.println(dr.getDecompiledFunction().getC());
            }
            di.dispose();
        } else {
            w.println("NO FN");
        }
        w.close();
        println("done");
    }
}
