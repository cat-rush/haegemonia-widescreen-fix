import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit5 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit5_report.txt"));
        long[][] ranges = {{0x0076be00L, 0x0076c3a0L}, {0x00775fb7L, 0x00776530L}};
        for (long[] rg : ranges) {
            w.println("==== disassembly " + Long.toHexString(rg[0]) + " .. " + Long.toHexString(rg[1]) + " ====");
            for (long a = rg[0]; a < rg[1]; ) {
                Instruction ins = getInstructionAt(toAddr(a));
                if (ins == null) { a += 1; continue; }
                w.println(ins.getAddress() + "  " + ins.toString());
                a += ins.getLength();
            }
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x00775fb7L));
        if (f != null) {
            w.println("======== FUN_00775fb7 decompile ========");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 60000) c = c.substring(0, 60000) + "\n... TRUNCATED ...";
                w.println(c);
            } else w.println("DECOMP FAILED");
        }
        w.close();
        println("done");
    }
}
