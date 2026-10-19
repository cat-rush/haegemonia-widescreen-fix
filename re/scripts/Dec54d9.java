import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec54d9 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec54d9_report.txt"));
        long a = 0x0054d94dL;
        Function f = getFunctionContaining(toAddr(a));
        if (f == null) { w.println("NO FN"); w.close(); return; }
        w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults r = di.decompileFunction(f, 90, monitor);
        if (r != null && r.decompileCompleted()) {
            String c = r.getDecompiledFunction().getC();
            if (c.length() > 90000) c = c.substring(0, 90000) + "\n... TRUNCATED ...";
            w.println(c);
        } else {
            w.println("DECOMP FAILED");
        }
        w.close();
        println("done");
    }
}
