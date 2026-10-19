import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit8 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit8_report.txt"));
        long[][] ranges = {
            {0x0078795aL, 0x00787a40L},
            {0x007a16b4L, 0x007a1730L},
        };
        for (long[] rg : ranges) {
            w.println("==== disasm " + Long.toHexString(rg[0]) + " .. " + Long.toHexString(rg[1]) + " ====");
            for (long a = rg[0]; a < rg[1]; ) {
                Instruction ins = getInstructionAt(toAddr(a));
                if (ins == null) { a += 1; continue; }
                w.println(ins.getAddress() + "  " + ins.toString());
                a += ins.getLength();
            }
        }
        w.println("==== callers of FUN_007a16b4 ====");
        Reference[] refs = getReferencesTo(toAddr(0x007a16b4L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        w.println("==== callers of FUN_0078da5f / FUN_0076a6e4 / FUN_0077a24b ====");
        long[] fns = {0x0078da5fL, 0x0076a6e4L, 0x0077a24bL};
        for (long a : fns) {
            Reference[] r2 = getReferencesTo(toAddr(a));
            w.println("-- " + Long.toHexString(a) + " --");
            for (Reference ref : r2) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName();
                if (ref.getReferenceType().isCall())
                    w.println("   from " + ref.getFromAddress() + " in " + fname);
            }
        }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x007a16b4L));
        if (f != null) {
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted())
                w.println(r.getDecompiledFunction().getC());
        }
        w.close();
        println("done");
    }
}
