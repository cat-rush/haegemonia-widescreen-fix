import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec11 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec11_report.txt"));
        long[] dec = {0x007a8730L, 0x0051caa0L, 0x00715932L};
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
        // xrefs to FUN_0077efc8
        w.println("==================== XREFS TO FUN_0077efc8");
        Function f = getFunctionAt(toAddr(0x0077efc8L));
        if (f != null) {
            for (Reference ref : getReferencesTo(f.getEntryPoint())) {
                w.println(ref.getFromAddress().toString() + " type=" + ref.getReferenceType());
            }
        }
        // disassemble around the squad list writers
        long[][] ranges = { {0x0076c0a0L, 0x0076c2c0L}, {0x00776200L, 0x00776380L} };
        for (long[] r : ranges) {
            w.println("==================== DISASM " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]));
            Instruction inst = getInstructionAt(toAddr(r[0]));
            int n = 0;
            while (inst != null && inst.getAddress().getOffset() <= r[1] && n < 800) {
                w.println(inst.toString());
                inst = getInstructionAfter(inst);
                n++;
            }
        }
        w.close();
        println("done");
    }
}
