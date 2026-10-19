import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class OverChk extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\overchk_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x009FD166L));
        if (f != null) {
            w.println(f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            }
        } else {
            w.println("no function at 0x9FD166");
        }
        d.dispose();
        w.close();
        println("done");
    }
}
