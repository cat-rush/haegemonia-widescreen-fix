import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiHumanRow2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aihumanrow2.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] dec = { 0x7c99feL, 0x7d3dedL, 0x56ca64L };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }

        w.println("==== ASM FUN_007ace1b around the FUN_007c99fe call (0x7b4d43) ====");
        Address a = toAddr(0x7b4cc0L);
        while (a != null && a.getOffset() <= 0x7b4d90L) {
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
