import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class XrefQuick extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefquick_report.txt"));
        long[] addrs = {0x0071a9e0L, 0x00712755L, 0x007c3968L};
        for (long a : addrs) {
            w.println("==== xrefs to 0x" + Long.toHexString(a));
            int n = 0;
            for (Reference ref : getReferencesTo(toAddr(a))) {
                Function f = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + "  in " +
                          (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
                if (++n >= 30) { w.println("  ..."); break; }
            }
            if (n == 0) w.println("  none");
        }
        w.close();
        println("done");
    }
}
