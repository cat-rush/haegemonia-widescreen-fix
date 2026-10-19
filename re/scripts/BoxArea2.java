// BoxArea2.java - trace the box-scene vtable owners + screen-area drawers
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea2_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);

        long[] fns = {
            0x00711e94L, 0x00711c0aL, 0x0052f0ffL, 0x004b5c67L,
            0x0077efc8L, 0x007154d8L, 0x00713f0aL, 0x007f5123L,
            0x004ad440L, 0x004ae0c5L, 0x004aae79L, 0x004745d6L
        };
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

        // data references to the static vtable slots
        long[] slots = { 0x00b2ec7cL, 0x00b2ff1cL, 0x00b2feecL, 0x00b302f4L, 0x00b30554L, 0x00b30684L };
        for (long s : slots) {
            w.println("==== references to static vtable slot " + Long.toHexString(s) + " ====");
            try {
                ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(s));
                for (ghidra.program.model.symbol.Reference ref : refs) {
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("  " + ref.getReferenceType() + " from " + ref.getFromAddress() +
                              (cf != null ? " in " + cf.getName() : ""));
                }
            } catch (Exception e) { w.println("  EXC " + e.getMessage()); }
        }

        d.dispose();
        w.close();
        println("done: boxarea2_report.txt");
    }
}
