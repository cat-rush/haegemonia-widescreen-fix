import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgBoxDec6 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxdec6_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x0048cc8aL, 0x0048c5caL, 0x0048be3dL};
        for (long f : fns) {
            Address a = toAddr(f);
            w.println("=== DECOMP 0x" + Long.toHexString(f));
            DecompileResults r = ifc.decompileFunction(getFunctionAt(a), 300, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("FAILED");
            }
        }
        w.close();
        println("done");
    }
}
