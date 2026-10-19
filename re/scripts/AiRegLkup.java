import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiRegLkup extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aireglkup_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] addrs = {0x55561bL, 0x56cc03L, 0x56cc59L};
        for (long ad : addrs) {
            Function f = getFunctionContaining(toAddr(ad));
            w.println("========== DECOMP 0x" + Long.toHexString(ad) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }
        w.println("==== ASM 0x55561b .. 0x555800 ====");
        Address a = toAddr(0x55561bL);
        int guard = 0;
        while (a != null && a.getOffset() <= 0x555800L && guard++ < 400) {
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
