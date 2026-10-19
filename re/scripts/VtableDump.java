import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Data;
import ghidra.program.model.mem.MemoryBlock;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtableDump extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtable_report.txt"));
        for (Reference r : getReferencesTo(toAddr(0x0050DB50L))) {
            Address from = r.getFromAddress();
            w.println("FUN_0050db50 referenced from " + from + " type=" + r.getReferenceType());
            Data d = getDataAt(from);
            if (d != null) {
                w.println("   data: " + d.toString());
            }
        }
        w.close();
        println("done");
    }
}
