import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec9 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec9_report.txt"));
        long[] addrs = {0x00427770L, 0x004242ecL, 0x00423bb0L, 0x00424335L, 0x004292c7L};
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==================== FUN @ " + Long.toHexString(a));
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
