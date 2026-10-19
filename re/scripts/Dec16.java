import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec16 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec16_report.txt"));
        long[] sites = {0x00540220L, 0x00540410L, 0x005405d0L, 0x00540790L, 0x005417c0L};
        for (long s : sites) {
            w.println("==================== DISASM near " + Long.toHexString(s));
            Instruction inst = getInstructionAt(toAddr(s));
            int n = 0;
            while (inst != null && n < 120) {
                w.println(inst.toString());
                inst = getInstructionAfter(inst);
                n++;
            }
        }
        w.close();
        println("done");
    }
}
