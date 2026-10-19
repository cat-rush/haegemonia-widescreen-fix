import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHelperDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aihelper_report.txt"));
        long[] fns = {0x0058a998L, 0x0058a99dL, 0x0058a9a1L, 0x00404a76L, 0x0066eb3bL};
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long t : fns) {
            Function f = getFunctionContaining(toAddr(t));
            w.println("");
            w.println("========== DECOMP 0x" + Long.toHexString(t) + (f != null ? " " + f.getName() : "") + " ==========");
            if (f == null) { w.println("no function"); continue; }
            DecompileResults res = di.decompileFunction(f, 60, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
        }
        w.close();
        println("done");
    }
}
