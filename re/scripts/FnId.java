import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnId extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnid_report.txt"));
        long[] addrs = {0x000ebdbbL, 0x000732b3L, 0x001b9984L, 0x000e929fL,
                        0x004e7cc1L, 0x0044728eL, 0x00447396L};
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
