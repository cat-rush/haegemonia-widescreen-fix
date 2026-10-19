import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.listing.Data;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

public class AiFill extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aifill_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        long[] fns = {0x007a7c80L, 0x006c4250L, 0x00505a7dL};
        for (long t : fns) {
            Function f = getFunctionContaining(toAddr(t));
            w.println("========== DECOMP " + Long.toHexString(t) + (f != null ? " " + f.getName() : "") + " ==========");
            if (f != null) {
                DecompileResults res = di.decompileFunction(f, 120, monitor);
                if (res != null && res.decompileCompleted()) {
                    w.println(res.getDecompiledFunction().getC());
                } else {
                    w.println("!!! decompile failed");
                }
            }
        }
        w.println("");
        w.println("==== xrefs to widget builder 7a7c80 ====");
        Set<Function> fset = new LinkedHashSet<Function>();
        Reference[] refs = getReferencesTo(toAddr(0x007a7c80L));
        for (Reference r : refs) {
            Address from = r.getFromAddress();
            Function f = getFunctionContaining(from);
            w.println("  from " + from + (f != null ? " in " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            if (f != null) fset.add(f);
        }
        w.println("");
        w.println("==== vtable at 7bda04 (first 24 entries as ptrs) ====");
        Memory mem = currentProgram.getMemory();
        for (int i = 0; i < 24; i++) {
            Address a = toAddr(0x007bda04L + i * 4);
            try {
                int v = mem.getInt(a);
                w.println("  [" + i + "] " + a + " = 0x" + Integer.toHexString(v));
            } catch (Exception e) {
                w.println("  [" + i + "] unreadable");
            }
        }
        w.close();
        println("done");
    }
}
