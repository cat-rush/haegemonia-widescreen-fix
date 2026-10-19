import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgGetDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgget_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x71d0d1L, 0x720f31L, 0x7250b4L, 0x701e2cL, 0x6cdb1bL, 0x6cdc27L};
        for (long f : fns) {
            Function fn = getFunctionAt(toAddr(f));
            if (fn == null) { w.println("=== 0x" + Long.toHexString(f) + ": no function"); continue; }
            w.println("=== DECOMP 0x" + Long.toHexString(f) + " " + fn.getBody());
            DecompileResults r = ifc.decompileFunction(fn, 300, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("FAILED");
            }
        }
        w.println("==== xrefs to 0x71d0d1");
        Reference[] refs = getReferencesTo(toAddr(0x71d0d1L));
        for (Reference r : refs) {
            Address f = r.getFromAddress();
            Function fn = getFunctionContaining(f);
            w.println("   from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
        }
        w.close();
        println("done");
    }
}
