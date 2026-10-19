import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RowhXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rowhxref_report.txt"));
        int n = 0;
        for (Reference r : getReferencesTo(toAddr(0x004F21C2L))) {
            Address from = r.getFromAddress();
            Function cf = getFunctionContaining(from);
            w.println("from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
            n++;
        }
        w.println("total " + n);
        w.close();
        println("done");
    }
}
