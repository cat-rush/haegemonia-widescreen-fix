import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitA extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhita_report.txt"));
        long[] fns = {0x00780fa1L, 0x0079b2c7L};
        for (long a : fns) {
            w.println("==== refs to " + Long.toHexString(a) + " ====");
            Reference[] refs = getReferencesTo(toAddr(a));
            for (Reference ref : refs) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName();
                w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
            }
        }
        w.println("==== disasm FUN_0079b2c7 (caller of FUN_00799b3e?) ====");
        Function f = getFunctionAt(toAddr(0x0079b2c7L));
        if (f != null) {
            for (long a = f.getEntryPoint().getOffset(); a < f.getBody().getMaxAddress().getOffset(); ) {
                Instruction ins = getInstructionAt(toAddr(a));
                if (ins == null) { a += 1; continue; }
                w.println(ins.getAddress() + "  " + ins.toString());
                a += ins.getLength();
                if (a > f.getEntryPoint().getOffset() + 0x120) break;
            }
        }
        w.close();
        println("done");
    }
}
