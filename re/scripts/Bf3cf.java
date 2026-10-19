import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Bf3cf extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bf3cf_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        Function f = getFunctionContaining(toAddr(0x004bf3cfL));
        if (f != null) {
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
        }
        d.dispose();
        w.close();
        println("done");
    }
}
