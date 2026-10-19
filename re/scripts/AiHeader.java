import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHeader extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aiheader.txt"));
        long[][] ranges = { {0x7c3c70L, 0x7c3da5L} };
        for (long[] r : ranges) {
            w.println("==== ASM " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]) + " ====");
            Address a = toAddr(r[0]);
            while (a != null && a.getOffset() <= r[1]) {
                disassemble(a);
                Instruction inst = getInstructionAt(a);
                if (inst != null) {
                    w.println("  " + inst.getAddress() + " " + inst.toString());
                    a = inst.getAddress().add(inst.getLength());
                } else {
                    w.println("  " + a + " (no instr)");
                    a = a.add(1);
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
