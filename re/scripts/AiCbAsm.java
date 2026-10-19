import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiCbAsm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aicbasm.txt"));
        long[][] ranges = { {0x7cb540L, 0x7cb640L}, {0x7cb680L, 0x7cb6e0L}, {0x7cc820L, 0x7cc870L},
                            {0x7cc742L, 0x7cc780L}, {0x7beb80L, 0x7beba5L} };
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
