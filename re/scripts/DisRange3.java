import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisRange3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\disrange3_report.txt"));
        long[][] ranges = { {0x000e9000L, 0x000e9400L}, {0x000ebd00L, 0x000ec100L},
                            {0x00073180L, 0x00073340L}, {0x001b9800L, 0x001b9a40L} };
        for (long[] r : ranges) {
            w.println("===== range " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]) + " =====");
            Address a = toAddr(r[0]);
            Address end = toAddr(r[1]);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
            int n = 0;
            while (it.hasNext()) {
                Instruction ins = it.next();
                if (ins.getAddress().compareTo(end) > 0) break;
                w.println(ins.getAddress() + "  " + ins);
                if (++n > 400) { w.println("... (truncated)"); break; }
            }
        }
        w.close();
        println("done");
    }
}
