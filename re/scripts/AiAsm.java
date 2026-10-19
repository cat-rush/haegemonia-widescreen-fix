import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiAsm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aiasm.txt"));
        long[][] ranges = { {0x828ff4L, 0x829030L}, {0x56cd8dL, 0x56cdb4L}, {0x5552bcL, 0x5552d5L},
                            {0x68fc30L, 0x68fcbbL}, {0x7bf40dL, 0x7bf475L}, {0x7cc800L, 0x7cc84fL},
                            {0x7cc75eL, 0x7cc785L}, {0x7cc609L, 0x7cc660L} };
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
