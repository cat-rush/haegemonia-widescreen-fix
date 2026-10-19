import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RowXref extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rowxref_report.txt"));
        Address t = toAddr(0x004f21c2L);
        Reference[] refs = getReferencesTo(t);
        w.println("callers of FUN_004f21c2: " + (refs != null ? refs.length : 0));
        if (refs != null) {
            for (Reference r : refs) {
                if (r.getReferenceType().isCall()) {
                    w.println("   CALL from " + r.getFromAddress());
                }
            }
        }
        w.close();
        println("done");
    }
}
