import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitL extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitl_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x0077a24bL));
        if (f == null) f = getFunctionContaining(toAddr(0x0077a24bL));
        w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
        DecompileResults r = di.decompileFunction(f, 90, monitor);
        if (r != null && r.decompileCompleted()) {
            String c = r.getDecompiledFunction().getC();
            if (c.length() > 70000) c = c.substring(0, 70000) + "\n... TRUNCATED ...";
            w.println(c);
        } else {
            w.println("DECOMP FAILED");
        }
        w.close();
        println("done");
    }
}
