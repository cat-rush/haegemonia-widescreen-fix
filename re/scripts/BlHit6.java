import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit6 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit6_report.txt"));
        long[][] ranges = {
            {0x0078dfa0L, 0x0078dfd5L},
            {0x0078e5a0L, 0x0078e5e0L},
            {0x0078e5f0L, 0x0078e630L},
            {0x0078bf20L, 0x0078bf80L},
            {0x0078dc2eL, 0x0078dca0L},
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
        long[] fns = {0x0078dc2eL, 0x0078bd79L, 0x0079aea2L};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("==== callers of " + f.getName() + " ====");
            Reference[] refs = getReferencesTo(f.getEntryPoint());
            for (Reference ref : refs) {
                if (ref.getReferenceType().isCall()) {
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    String fname = cf == null ? "?" : cf.getName();
                    w.println("   from " + ref.getFromAddress() + " in " + fname);
                }
            }
        }
        w.close();
        println("done");
    }
}
