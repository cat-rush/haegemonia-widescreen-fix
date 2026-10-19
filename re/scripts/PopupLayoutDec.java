import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PopupLayoutDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popuplayout_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x00525bf5L, 0x00526ce8L, 0x00527752L, 0x00529886L,
                         0x0053db70L, 0x00538461L, 0x00529ca2L, 0x00529c29L,
                         0x0053c550L, 0x0053bfdaL};
        for (long s : starts) {
            Function f = getFunctionContaining(toAddr(s));
            if (f == null) { w.println("=== no fn at 0x" + Long.toHexString(s)); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = ifc.decompileFunction(f, 300, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("DECOMPILE FAILED");
            }
        }
        w.close();
        println("done");
    }
}
