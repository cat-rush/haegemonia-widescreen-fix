import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrBab extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brbab_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x004bab34L, 0x004a0166L, 0x004a8aefL };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("==== addr 0x" + Long.toHexString(a) + " in " + (f != null ? f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ")" : "(none)") + " ====");
            if (f != null) {
                ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
                w.println("callers:");
                for (ghidra.program.model.symbol.Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
                }
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null)
                    w.println(r.getDecompiledFunction().getC());
                else w.println("(decompile failed)");
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
