import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiJobFns extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aijobfns.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] dec = { 0x52a0cfL, 0x7d4e10L, 0x7d4e30L, 0x7cec54L };
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

        w.println("==== ASM 0x7c3d80 .. 0x7c3e60 ====");
        Address a = toAddr(0x7c3d80L);
        while (a != null && a.getOffset() <= 0x7c3e60L) {
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
        w.close();
        println("done");
    }
}
