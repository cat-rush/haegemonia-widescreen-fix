// BoxArea5.java - find the box-container position: all designW readers,
// the camera getter FUN_0047e427, and the owner of vtable 0xb31b10.
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea5 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea5_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);

        w.println("==== all code refs to 0xc0a790 (designW) ====");
        try {
            Reference[] refs = getReferencesTo(toAddr(0xc0a790L));
            for (Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " @ " + cf.getEntryPoint() : ""));
            }
        } catch (Exception e) { w.println("  EXC " + e.getMessage()); }

        w.println("==== data refs to 0xb31b10 (FUN_0052f0ff vtable slot) ====");
        try {
            Reference[] refs = getReferencesTo(toAddr(0xb31b10L));
            for (Reference ref : refs) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
        } catch (Exception e) { w.println("  EXC " + e.getMessage()); }

        long[] fns = { 0x0047e427L, 0x0052f0ffL, 0x004ba962L };
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
        println("done: boxarea5_report.txt");
    }
}
