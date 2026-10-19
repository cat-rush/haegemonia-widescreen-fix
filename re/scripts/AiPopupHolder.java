import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiPopupHolder extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aipopupholder.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        w.println("===== HOLDER VTABLE DUMP 0xb2eb14 =====");
        for (int i = 0; i < 24; i++) {
            long a = 0xb2eb14L + i * 4;
            int v = getInt(toAddr(a));
            Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
            w.println(String.format("  [%2d] 0x%08X -> %s", i, v,
                f != null ? f.getName() + " @ " + f.getEntryPoint() : "?"));
        }
        w.println("");

        long[] dec = { 0x7bda04L, 0x7bebf4L, 0x7b82d9L, 0x7bb9aeL, 0x7bcf4dL, 0x7bd335L, 0x7bf616L, 0x7c99feL };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
