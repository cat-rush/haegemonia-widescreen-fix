import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiPopupMth extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aipopmth_report.txt"));
        long[] fns = {0x45bf60L, 0x45c1f0L, 0x45e000L, 0x45eef0L, 0x45e350L, 0x45e5e0L,
                      0x4cd0b0L, 0x599280L, 0x726ac0L, 0x45e710L, 0x5958c0L, 0x45e9d0L,
                      0x6ffae0L, 0x45eb80L, 0x45ed30L, 0x594f00L, 0x460590L, 0x45b804L,
                      0x45b870L, 0x45b880L, 0x45b920L, 0x45b970L, 0x45b9b0L, 0x45b9f0L,
                      0x45bff0L, 0x459629L, 0x459657L, 0x45960fL, 0x459705L, 0x459c21L,
                      0x5913a4L, 0x59b040L, 0x59b081L, 0x59b137L, 0x59b16aL, 0x59b4d0L};
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
