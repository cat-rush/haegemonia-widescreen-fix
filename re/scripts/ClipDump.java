import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ClipDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(f != null ? f.getEntryPoint().getOffset() : va) + " ----");
        if (f != null) w.println("range: " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    void xrefs(long va, String title) throws Exception {
        w.println("== " + title + " ==");
        Reference[] refs = getReferencesTo(toAddr(va));
        if (refs != null) {
            for (Reference r : refs) {
                if (r.getReferenceType().isCall()) w.println("   CALL from " + r.getFromAddress());
                else w.println("   ref from " + r.getFromAddress());
            }
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\clipdump_report.txt"));
        dec(0x0044864eL);       /* renderer SetRect */
        xrefs(0x0044864eL, "callers of 44864e");
        dec(0x00536a0fL);       /* research/planet screen */
        w.close();
        println("done");
    }
}
