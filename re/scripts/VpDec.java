// VpDec.java - decompile the viewport setters 0x43FCAa/0x43FBE0 + helpers
// 0x43F0D1, 0x446C62, 0x4AAEED, 0x4B6FFC + FUN_004aae79. Report -> vpdec_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VpDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vpdec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x43FCAA, 0x43FBE0, 0x43F0D1, 0x446C62, 0x4AAEED, 0x4B6FFC, 0x4AAE79 };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); continue; }
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            Reference[] refs = getReferencesTo(f.getEntryPoint());
            w.println("callers:");
            for (Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
            else w.println("(decompile failed)");
        }
        d.dispose();
        w.close();
        println("done: vpdec_report.txt");
    }
}
