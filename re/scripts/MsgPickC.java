import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgPickC extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgpickc_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x5357a9L, 0x53582dL, 0x535955L};
        for (long f : fns) {
            Function fn = getFunctionAt(toAddr(f));
            if (fn == null) { w.println("=== 0x" + Long.toHexString(f) + ": no function"); continue; }
            w.println("=== DECOMP 0x" + Long.toHexString(f) + " " + fn.getBody());
            DecompileResults r = ifc.decompileFunction(fn, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            }
            w.println("--- callers:");
            Reference[] refs = getReferencesTo(fn.getEntryPoint());
            for (Reference rr : refs) {
                Address a = rr.getFromAddress();
                Function cf = getFunctionContaining(a);
                w.println("   from " + a + (cf != null ? " fn=" + cf.getName() + " " + cf.getBody() : ""));
            }
        }
        w.close();
        println("done");
    }
}
