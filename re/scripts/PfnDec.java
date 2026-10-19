import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PfnDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pfndec_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Address a = toAddr(0x00536a0fL);
        w.println("=== DECOMP 0x536a0f (planet/message text fn)");
        DecompileResults r = ifc.decompileFunction(getFunctionAt(a), 300, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("FAILED");
        }
        w.close();
        println("done");
    }
}
