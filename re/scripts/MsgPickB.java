import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgPickB extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgpickb_report.txt"));
        long[] fns = {0x5356e8L, 0x53572bL};
        for (long f : fns) {
            w.println("==== callers of 0x" + Long.toHexString(f));
            Reference[] refs = getReferencesTo(toAddr(f));
            for (Reference r : refs) {
                Address a = r.getFromAddress();
                Function fn = getFunctionContaining(a);
                w.println("   from " + a + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
            }
        }
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Function fn = getFunctionAt(toAddr(0x5356e8L));
        if (fn != null) {
            w.println("=== DECOMP FUN_005356e8 " + fn.getBody());
            DecompileResults r = ifc.decompileFunction(fn, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            }
        }
        w.close();
        println("done");
    }
}
