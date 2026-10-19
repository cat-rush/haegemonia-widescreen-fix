import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
public class BlHitU extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitu_report.txt"));
        long[] as = {0x00539b69L, 0x00535a00L, 0x0077a30aL};
        for (long a : as) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) w.println(Long.toHexString(a) + " -> " + f.getName() + " [" + f.getEntryPoint() + ".." + f.getBody().getMaxAddress() + "]");
            else w.println(Long.toHexString(a) + " -> NO FN");
        }
        w.close();
        println("done");
    }
}
