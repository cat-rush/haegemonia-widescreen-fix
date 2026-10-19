import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitB extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitb_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x004a04fbL, 0x004a89f3L, 0x007a37f6L, 0x0079aea2L};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 40000) c = c.substring(0, 40000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
        }
        w.println("==== refs to FUN_007a37f6 ====");
        Reference[] refs = getReferencesTo(toAddr(0x007a37f6L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        w.println("==== disasm 7a37f6 prologue ====");
        for (long a = 0x007a37f6L; a < 0x007a3840L; ) {
            Instruction ins = getInstructionAt(toAddr(a));
            if (ins == null) { a += 1; continue; }
            w.println(ins.getAddress() + "  " + ins.toString());
            a += ins.getLength();
        }
        w.close();
        println("done");
    }
}
