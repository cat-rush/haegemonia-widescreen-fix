import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LoadDis extends GhidraScript {
    PrintWriter w;
    void dis(long va, long end) throws Exception {
        Address a = toAddr(va);
        while (a.getOffset() < end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins.getAddress() + "  " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\loaddis_report.txt"));
        w.println("== disasm FUN_00717243 0x717243..0x717BA5 ==");
        dis(0x00717243L, 0x00717BA5L);
        w.println("");
        w.println("== disasm FUN_00717BA5 0x717BA5..0x7188D2 ==");
        dis(0x00717BA5L, 0x007188D2L);
        w.println("");
        w.println("== decompile ==");
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        for (long a : new long[] { 0x00717243L, 0x00717BA5L }) {
            Function f = getFunctionAt(toAddr(a));
            if (f != null) {
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                w.println("---- " + f.getName() + " @ " + Long.toHexString(a) + " ----");
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                } else {
                    w.println("decompile failed");
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
