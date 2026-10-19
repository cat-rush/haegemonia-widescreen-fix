import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VerSrcDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\versrc_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x005ac1c3L, 0x006fefe0L, 0x0058aec9L, 0x0058af0eL};
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
