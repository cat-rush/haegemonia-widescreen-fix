import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec13 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec13_report.txt"));
        long[] dec = {0x007e84eeL, 0x007e862eL, 0x007e9839L, 0x0056c8fbL, 0x007e8d65L};
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        for (long a : dec) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==================== DEC FUN @ " + Long.toHexString(a));
            if (f == null) { w.println("NO FUNCTION"); continue; }
            w.println("name: " + f.getName() + "  body: " + f.getBody());
            DecompileResults r = ifc.decompileFunction(f, 180, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed");
            }
        }
        w.close();
        println("done");
    }
}
