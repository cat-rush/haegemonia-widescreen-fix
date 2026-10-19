import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LabelHelpersDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\labelhelpers_report.txt"));
        long[][] ranges = {
            {0x006495e0L, 0x006497ffL},
            {0x007885f2L, 0x007886ffL},
            {0x0077ee70L, 0x0077f0ffL},
            {0x0077ed6aL, 0x0077ee70L},
            {0x005d5b04L, 0x005d5cffL},
            {0x00541c80L, 0x00541effL}
        };
        for (long[] r : ranges) {
            FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
            while (it.hasNext()) {
                Function f = it.next();
                long e = f.getEntryPoint().getOffset();
                if (e >= r[0] && e <= r[1]) {
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
