import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnRange extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnrange_report.txt"));
        long[] addrs = {0x0075c69dL, 0x0075c749L, 0x0075c98bL, 0x007a5f85L, 0x005400cfL};
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println(Long.toHexString(a) + " -> " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            } else {
                w.println(Long.toHexString(a) + " -> NO FN");
            }
        }
        w.close();
        println("done");
    }
}
