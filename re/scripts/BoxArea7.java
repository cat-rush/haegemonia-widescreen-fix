// BoxArea7.java - projectors' data refs + owner decompiles
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea7 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea7_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);

        long[] projs = { 0x00475a82L, 0x00475c07L, 0x00475d9eL, 0x00475f2dL, 0x0047609aL };
        for (long p : projs) {
            w.println("==== data refs to projector " + Long.toHexString(p) + " ====");
            try {
                Reference[] refs = getReferencesTo(toAddr(p));
                for (Reference ref : refs) {
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
                }
            } catch (Exception e) { w.println("  EXC " + e.getMessage()); }
        }

        long[] fns = { 0x0047609aL, 0x00551d79L, 0x006dd5e9L };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); continue; }
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
            w.println("callers:");
            for (ghidra.program.model.symbol.Reference ref : refs) {
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
        println("done: boxarea7_report.txt");
    }
}
