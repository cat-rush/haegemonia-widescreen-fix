import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiBigDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aibigdec_report.txt"));
        Function f = getFunctionContaining(toAddr(0x007ace1bL));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        w.println("decompiling " + f.getName() + " ...");
        long t0 = System.currentTimeMillis();
        DecompileResults res = di.decompileFunction(f, 600, monitor);
        w.println("took " + (System.currentTimeMillis()-t0) + " ms, completed=" + (res != null && res.decompileCompleted()));
        if (res != null && res.decompileCompleted()) {
            w.println(res.getDecompiledFunction().getC());
        }
        w.close();
        println("done");
    }
}
