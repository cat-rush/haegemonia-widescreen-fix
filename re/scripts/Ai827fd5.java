import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai827fd5 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai827fd5.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        Function f = getFunctionContaining(toAddr(0x827fd5L));
        w.println("========== DECOMP 0x827fd5 ==========");
        w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
        if (f != null) {
            w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = di.decompileFunction(f, 240, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
        }
        w.println("");
        w.println("==== ASM 0x827fd5 .. 0x827ff2 ====");
        Address a = toAddr(0x827fd5L);
        while (a != null && a.getOffset() <= 0x827ff2L) {
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
