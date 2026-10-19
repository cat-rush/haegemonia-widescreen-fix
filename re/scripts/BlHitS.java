import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitS extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhits_report.txt"));
        long[] sites = {0x0070c6afL, 0x0079d903L, 0x0079e18aL, 0x0079e853L, 0x0079f68aL};
        for (long s : sites) {
            w.println("==== around " + Long.toHexString(s) + " ====");
            for (long a = s - 0x20; a < s + 0x10; ) {
                Instruction ins = getInstructionAt(toAddr(a));
                if (ins == null) { a += 1; continue; }
                w.println(ins.getAddress() + "  " + ins.toString());
                a += ins.getLength();
            }
        }
        w.close();
        println("done");
    }
}
