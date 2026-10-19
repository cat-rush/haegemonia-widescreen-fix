import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgUpd extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgupd_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x004c0ea3L, 0x004c1504L, 0x0047e650L};
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
