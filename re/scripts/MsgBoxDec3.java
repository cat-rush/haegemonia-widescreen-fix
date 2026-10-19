import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgBoxDec3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxdec3_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x0053846bL, 0x0054f05aL};
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
