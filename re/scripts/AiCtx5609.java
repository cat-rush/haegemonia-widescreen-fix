import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiCtx5609 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai5609_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] addrs = {0x560900L, 0x7d304bL, 0x7a9540L};
        for (long ad : addrs) {
            Function f = getFunctionContaining(toAddr(ad));
            w.println("========== FN containing 0x" + Long.toHexString(ad) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() + " body " + f.getBody() : "none"));
            if (f != null) {
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }
        w.println("==== ASM around 0x5608c0 .. 0x560960 ====");
        Address a = toAddr(0x5608c0L);
        int guard = 0;
        while (a != null && a.getOffset() <= 0x560960L && guard++ < 200) {
            disassemble(a);
            Instruction inst = getInstructionAt(a);
            if (inst != null) {
                w.println("  " + inst.getAddress() + " " + inst.toString());
                a = inst.getAddress().add(inst.getLength());
            } else {
                a = a.add(1);
            }
        }
        w.close();
        println("done");
    }
}
