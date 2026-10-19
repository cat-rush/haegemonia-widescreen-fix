import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiBda04Asm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aibda04asm.txt"));
        long[][] ranges = { {0x7bda29L, 0x7bdba0L}, {0x7be8f0L, 0x7bea00L} };
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
