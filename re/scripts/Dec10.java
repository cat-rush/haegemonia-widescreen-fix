import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec10 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec10_report.txt"));
        long[] dec = {0x0077efc8L, 0x0076bb6eL, 0x0076bc4aL};
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        for (long a : dec) {
            Function f = getFunctionAt(toAddr(a));
            w.println("==================== DEC FUN @ " + Long.toHexString(a));
            if (f == null) { w.println("NO FUNCTION"); continue; }
            w.println("name: " + f.getName() + "  body: " + f.getBody());
            DecompileResults r = ifc.decompileFunction(f, 180, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed");
            }
        }
        // disassemble FUN_0077efc8
        Function f = getFunctionAt(toAddr(0x0077efc8L));
        if (f != null) {
            w.println("==================== DISASM FUN @ 77efc8");
            Instruction inst = getFirstInstruction(f);
            int n = 0;
            while (inst != null && f.getBody().contains(inst.getAddress()) && n < 4000) {
                w.println(inst.toString());
                inst = getInstructionAfter(inst);
                n++;
            }
        }
        w.close();
        println("done");
    }
}
