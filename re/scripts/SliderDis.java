import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SliderDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\sliderdis_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0050DB50L));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        DecompileResults r = d.decompileFunction(f, 120, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("decompile failed");
        }
        d.dispose();
        w.close();
        println("done");
    }
}
