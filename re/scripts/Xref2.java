import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Xref2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xref2_report.txt"));
        for (Reference r : getReferencesTo(toAddr(0x00518BB1L))) {
            Address from = r.getFromAddress();
            Function cf = getFunctionContaining(from);
            w.println("from " + from + " type=" + r.getReferenceType() + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
        }
        w.close();
        println("done");
    }
}
