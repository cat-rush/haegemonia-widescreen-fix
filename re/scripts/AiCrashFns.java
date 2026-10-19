import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiCrashFns extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aicrashfns.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] dec = { 0x4cb6eeL, 0x43fbdeL, 0x44ee79L, 0x7c3f30L };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== containing 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }

        w.println("==== ASM around 0x7c3f30 (call site in FUN_007c3968) ====");
        Instruction inst = getInstructionAt(toAddr(0x7c3f00L));
        while (inst != null && inst.getAddress().getOffset() <= 0x7c3f70L) {
            w.println("  " + inst.getAddress() + " " + inst.toString());
            inst = inst.getNext();
        }
        w.println("");
        w.close();
        println("done");
    }
}
