import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class OptFn extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\optfn_report.txt"));
        for (long a : new long[] { 0x0052A1F8L, 0x0052A183L, 0x0052286BL }) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println(String.format("0x%08X in %s @ %s .. %s", a, f.getName(), f.getEntryPoint(), f.getBody().getMaxAddress()));
                w.println("  callers:");
                for (Reference r : getReferencesTo(f.getEntryPoint())) {
                    if (r.getReferenceType().isCall()) {
                        Address from = r.getFromAddress();
                        Function cf = getFunctionContaining(from);
                        w.println("    from " + from + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
                    }
                }
            } else {
                w.println(String.format("0x%08X: no function", a));
            }
        }
        w.close();
        println("done");
    }
}
