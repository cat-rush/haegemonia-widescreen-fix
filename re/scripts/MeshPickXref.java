import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MeshPickXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\meshpickxref_report.txt"));
        long[] fns = {0x4a04fbL, 0x4a89f3L, 0x4a0119L};
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
