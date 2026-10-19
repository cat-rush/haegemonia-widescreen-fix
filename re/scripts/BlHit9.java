import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit9 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit9_report.txt"));
        long[][] ranges = {
            {0x00781860L, 0x00781960L},
            {0x00784ea0L, 0x00784fd0L},
        };
        for (long[] rg : ranges) {
            w.println("==== disasm " + Long.toHexString(rg[0]) + " .. " + Long.toHexString(rg[1]) + " ====");
            for (long a = rg[0]; a < rg[1]; ) {
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
