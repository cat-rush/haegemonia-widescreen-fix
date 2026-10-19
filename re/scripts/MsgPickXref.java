import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgPickXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgpickxref_report.txt"));
        long[] fns = {0x54f002L, 0x51caa0L, 0x54f5c9L, 0x54f633L, 0x50e183L, 0x4f42fbL};
        for (long f : fns) {
            w.println("==== callers of 0x" + Long.toHexString(f));
            Reference[] refs = getReferencesTo(toAddr(f));
            for (Reference r : refs) {
                Address a = r.getFromAddress();
                Function fn = getFunctionContaining(a);
                w.println("   from " + a + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
            }
        }
        w.close();
        println("done");
    }
}
