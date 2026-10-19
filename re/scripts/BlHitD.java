import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitD extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitd_report.txt"));
        long[][] ranges = {{0x007a36d8L, 0x007a3700L}, {0x007a367eL, 0x007a368aL}};
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
