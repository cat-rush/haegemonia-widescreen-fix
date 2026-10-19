import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgWinDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgwin_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);

        // callers of the message window draw + its callers
        long[] fns = {0x788e0aL, 0x71eb21L, 0x54dfddL, 0x54e90fL};
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

        // xrefs to 0x788e0a (who calls the message window draw)
        w.println("==== callers of 0x788e0a");
        Reference[] refs = getReferencesTo(toAddr(0x788e0aL));
        for (Reference r : refs) {
            Address f = r.getFromAddress();
            Function fn = getFunctionContaining(f);
            w.println("   from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
        }
        w.println("==== callers of 0x71d214 (message screen ctor)");
        refs = getReferencesTo(toAddr(0x71d214L));
        for (Reference r : refs) {
            Address f = r.getFromAddress();
            Function fn = getFunctionContaining(f);
            w.println("   from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
        }
        w.println("==== callers of 0x54a8d0 (cGeneralMessages ctor)");
        refs = getReferencesTo(toAddr(0x54a8d0L));
        for (Reference r : refs) {
            Address f = r.getFromAddress();
            Function fn = getFunctionContaining(f);
            w.println("   from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
        }
        w.close();
        println("done");
    }
}
