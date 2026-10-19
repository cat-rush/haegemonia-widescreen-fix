import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class StripDump extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\stripdump_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        for (long a : new long[] { 0x0054F002L, 0x0054E90FL }) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                w.println("---- " + f.getName() + " @ " + f.getEntryPoint() + " ----");
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
