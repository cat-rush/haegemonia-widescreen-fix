import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiVtDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aivtdec_report.txt"));
        long[] fns = {0x439250L, 0x42E350L, 0x42E330L, 0x45BFB0L, 0x4302D0L, 0x430230L,
                      0x431DE0L, 0x430630L, 0x4308E0L, 0x430A10L, 0x430D00L, 0x431000L,
                      0x431020L, 0x431340L, 0x431570L, 0x431890L, 0x431700L, 0x431A50L,
                      0x431C20L, 0x594F00L, 0x433090L};
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long t : fns) {
            Function f = getFunctionContaining(toAddr(t));
            w.println("");
            w.println("========== DECOMP 0x" + Long.toHexString(t) + (f != null ? " " + f.getName() : "") + " ==========");
            if (f == null) { w.println("no function"); continue; }
            DecompileResults res = di.decompileFunction(f, 90, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("!!! decompile failed");
            }
        }
        w.close();
        println("done");
    }
}
