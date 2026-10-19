import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitC extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitc_report.txt"));
        long[][] ranges = {
            {0x007a35f0L, 0x007a36e0L},   /* FUN_007a34e0 pick calls */
            {0x007a38f0L, 0x007a3a00L},   /* FUN_007a37f6 pick calls */
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
