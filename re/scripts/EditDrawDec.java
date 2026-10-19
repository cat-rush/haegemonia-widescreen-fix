import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class EditDrawDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\editdraw_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x005074c9L, 0x005059ecL, 0x00505118L, 0x005054d2L, 0x00505c2aL};
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
