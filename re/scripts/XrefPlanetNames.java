import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class XrefPlanetNames extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\planetnames_xref_report.txt"));
        long[] addrs = {0x00516877L, 0x0084f61bL, 0x0066b227L, 0x00542394L};
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            String nm = (f != null) ? f.getName() : ("0x" + Long.toHexString(a));
            w.println("==== refs to " + nm + " (" + Long.toHexString(a) + ") ====");
            Reference[] refs = getReferencesTo(toAddr(a));
            for (Reference ref : refs) {
                if (ref.getReferenceType().isCall()) {
                    w.println("   from " + ref.getFromAddress());
                }
            }
        }
        w.close();
        println("done");
    }
}
