import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnRange2 extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnrange2_report.txt"));
        long[] addrs = {0x007176d7L, 0x00717f05L, 0x00717f7bL, 0x007181a7L, 0x00718332L, 0x0071839bL,
                        0x007a5f85L, 0x007a5fe7L, 0x0075bfa3L};
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
