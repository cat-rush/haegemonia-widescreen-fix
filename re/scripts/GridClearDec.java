import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GridClearDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\gridclear_report.txt"));
        long[][] ranges = {
            {0x0077ed4bL, 0x0077ed6aL}
        };
        for (long[] r : ranges) {
            FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
            while (it.hasNext()) {
                Function f = it.next();
                long e = f.getEntryPoint().getOffset();
                if (e >= r[0] && e < r[1]) {
                    w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
                    DecompInterface ifc = new DecompInterface();
                    ifc.openProgram(currentProgram);
                    DecompileResults res = ifc.decompileFunction(f, 300, monitor);
                    if (res != null && res.decompileCompleted()) {
                        w.println(res.getDecompiledFunction().getC());
                    } else {
                        w.println("DECOMPILE FAILED");
                    }
                    ifc.dispose();
                }
            }
        }
        w.close();
        println("done");
    }
}
