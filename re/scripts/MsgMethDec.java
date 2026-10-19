import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgMethDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgmeth_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x727620L, 0x726700L, 0x726990L, 0x726ac0L, 0x726d80L, 0x727040L, 0x7271f0L, 0x7273a0L, 0x729840L, 0x72b130L, 0x72b0e0L};
        for (long f : fns) {
            Function fn = getFunctionAt(toAddr(f));
            if (fn == null) { w.println("=== 0x" + Long.toHexString(f) + ": no function"); continue; }
            w.println("=== DECOMP 0x" + Long.toHexString(f) + " " + fn.getBody());
            DecompileResults r = ifc.decompileFunction(fn, 200, monitor);
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
