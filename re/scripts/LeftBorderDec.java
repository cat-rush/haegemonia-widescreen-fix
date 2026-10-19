import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LeftBorderDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\leftborder_report.txt"));
        long[] pts = {0x71AA5DL, 0x7C3E24L, 0x7C3F30L, 0x7C4013L};
        for (long p : pts) {
            Function f = getFunctionContaining(toAddr(p));
            w.println("==== 0x" + Long.toHexString(p) + " -> " +
                      (f != null ? f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() : "none"));
            if (f != null) {
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
        w.close();
        println("done");
    }
}
