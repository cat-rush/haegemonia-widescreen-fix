import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FontXref extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    void xrefs(long target, String title) throws Exception {
        w.println("== " + title + " ==");
        Address t = toAddr(target);
        Reference[] refs = getReferencesTo(t);
        w.println("   refs: " + (refs != null ? refs.length : 0));
        if (refs != null) {
            for (Reference r : refs) {
                if (r.getReferenceType().isCall()) {
                    w.println("   CALL from " + r.getFromAddress());
                } else {
                    w.println("   ref from " + r.getFromAddress());
                }
            }
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fontxref_report.txt"));
        xrefs(0x004390fdL, "callers of FUN_004390fd (font scale recompute)");
        xrefs(0x00435da3L, "callers of FUN_00435da3 (font loader)");
        w.println("== disasm of the 4390fd caller loop region 0x4390A0..0x439200 ==");
        Address a = toAddr(0x004390A0L);
        while (a.getOffset() < 0x00439200L) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done");
    }
}
