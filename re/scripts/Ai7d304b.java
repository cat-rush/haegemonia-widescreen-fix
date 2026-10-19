import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai7d304b extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai7d304b.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] dec = { 0x7d304bL, 0x7d2f84L, 0x5e47f0L, 0x4f65c9L };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 180, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }

        w.println("==== ASM 0x7bea00 .. 0x7bebc3 (case 7 tail) ====");
        Instruction inst = getInstructionAt(toAddr(0x7bea00L));
        while (inst != null && inst.getAddress().getOffset() <= 0x7bebc3L) {
            w.println("  " + inst.getAddress() + " " + inst.toString());
            inst = inst.getNext();
        }
        w.println("");
        w.close();
        println("done");
    }
}
