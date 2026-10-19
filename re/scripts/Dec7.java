import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec7 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec7_report.txt"));
        // disassemble around the crash
        Address a = toAddr(0x0072c590L);
        for (int i = 0; i < 0x80; i += 1) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println(ins.getAddress() + "  " + ins.toString());
            a = a.add(ins.getLength());
        }
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x0042b660L, 0x00435320L};
        for (long fa : fns) {
            Function f = getFunctionAt(toAddr(fa));
            w.println("==================== FUN @ " + Long.toHexString(fa));
            if (f == null) { w.println("NO FUNCTION"); continue; }
            w.println("name: " + f.getName() + "  body: " + f.getBody());
            DecompileResults r = ifc.decompileFunction(f, 120, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed");
            }
        }
        w.close();
        println("done");
    }
}
