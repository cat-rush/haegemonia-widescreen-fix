import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SquadDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\squaddec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x0077efc8L, 0x0077d5a3L, 0x0077ae14L };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==== " + Long.toHexString(a) + (f != null ? " " + f.getName() + " .. " + f.getBody().getMaxAddress() : " (no fn)") + " ====");
            if (f != null) {
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                } else {
                    w.println("(decompile failed)");
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
