import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class OptFn2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\optfn2_report.txt"));
        Function f = getFunctionContaining(toAddr(0x00525E9EL));
        w.println(f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        w.println("callers:");
        for (Reference r : getReferencesTo(f.getEntryPoint())) {
            if (r.getReferenceType().isCall()) {
                Address from = r.getFromAddress();
                Function cf = getFunctionContaining(from);
                w.println("   from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
            }
        }
        w.close();
        println("done");
    }
}
