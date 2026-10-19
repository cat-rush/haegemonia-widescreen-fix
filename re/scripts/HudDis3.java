import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HudDis3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\huddis_report.txt"));
        Function f = getFunctionAt(toAddr(0x0077EFC8L));
        w.println("FUN_0077efc8 = " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        Address a = toAddr(0x0077F880L);
        while (a.getOffset() < 0x0077F9C0L) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins.getAddress() + "  " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.println("== full decompile ==");
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        DecompileResults r = d.decompileFunction(f, 120, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("decompile failed");
        }
        d.dispose();
        w.close();
        println("done");
    }
}
