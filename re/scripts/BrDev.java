import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrDev extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brdev_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x00447337L, 0x00447176L, 0x004471f6L, 0x0044728eL };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
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
