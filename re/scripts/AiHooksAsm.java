import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHooksAsm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aihooksasm.txt"));
        long[][] ranges = { {0x7bda04L, 0x7bda40L}, {0x7beb90L, 0x7bebc3L},
                            {0x7cc3a0L, 0x7cc3f0L}, {0x7cb6a0L, 0x7cb6e0L},
                            {0x7d30e0L, 0x7d30fcL}, {0x5bb6f0L, 0x5bb760L},
                            {0x5bb640L, 0x5bb690L}, {0x7d1ef2L, 0x7d1f20L} };
        for (long[] r : ranges) {
            w.println("==== ASM " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]) + " ====");
            Instruction inst = getInstructionAt(toAddr(r[0]));
            while (inst != null && inst.getAddress().getOffset() <= r[1]) {
                w.println("  " + inst.getAddress() + " " + inst.toString());
                inst = inst.getNext();
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
