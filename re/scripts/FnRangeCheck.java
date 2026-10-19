import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnRangeCheck extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnrange_report.txt"));
        long[] pts = {0x7ce4f9L, 0x7ce91fL, 0x7cece1L, 0x7ceeebL, 0x7cef0aL, 0x7cd3f1L, 0x7ce33cL};
        for (long p : pts) {
            Function f = getFunctionContaining(toAddr(p));
            w.println("0x" + Long.toHexString(p) + " -> " +
                      (f != null ? f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() : "none"));
        }
        w.close();
        println("done");
    }
}
