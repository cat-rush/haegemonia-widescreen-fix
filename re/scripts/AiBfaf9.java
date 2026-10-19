import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiBfaf9 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aibfaf9_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionContaining(toAddr(0x007bfaf9L));
        w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
        if (f != null) {
            w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = di.decompileFunction(f, 180, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
        }
        w.close();
        println("done");
    }
}
