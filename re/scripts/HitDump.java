// HitDump.java - dump hit-test functions for the widescreen mod
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HitDump extends GhidraScript {

    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        w.println(dr.getDecompiledFunction().getC());
        w.flush();
        di.dispose();
    }

    void dis(long va, int maxBytes) throws Exception {
        w.println("== disasm @ " + Long.toHexString(va) + " ==");
        Address a = toAddr(va);
        int n = 0;
        while (n < maxBytes) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println(ins.toString());
            n += ins.getLength();
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }

    void callers(long va) throws Exception {
        w.println("== callers of " + Long.toHexString(va) + " ==");
        try {
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(toAddr(va));
            while (it.hasNext()) {
                Reference r = it.next();
                Function cf = getFunctionContaining(r.getFromAddress());
                w.println("   " + r.getReferenceType() + " from " + r.getFromAddress() +
                          " in " + (cf != null ? cf.getName() + " @ " + cf.getEntryPoint() : "?"));
            }
        } catch (Exception e) {
            w.println("   EXC: " + e.getMessage());
        }
        w.flush();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\hitdump_report.txt"));
        dec(0x00551ca5L);
        dis(0x00551ca5L, 0x300);
        callers(0x00551ca5L);
        callers(0x006cc5d5L);
        callers(0x0071357dL);
        callers(0x00518495L);
        callers(0x005356e8L);
        callers(0x00516542L);
        w.close();
        println("done: hitdump_report.txt");
    }
}
