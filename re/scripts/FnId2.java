import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnId2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnid2_report.txt"));
        long[] addrs = {0x005429e5L, 0x00542eb8L, 0x004e8616L, 0x004e8f61L, 0x004e9000L, 0x00541ef1L, 0x005400cfL, 0x006c3b4dL};
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
