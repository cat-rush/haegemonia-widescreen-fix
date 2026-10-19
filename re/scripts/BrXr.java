import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrXr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brxr_report.txt"));
        long[] addrs = { 0x0042c4a10L, 0x006cda2eL, 0x0042c6cb8L, 0x0042c5e10L };
        for (long a : addrs) {
            w.println("==== refs to 0x" + Long.toHexString(a) + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(a));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + ")" : " (no fn)"));
            }
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x006cda2eL, 0x006c4919L };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println("==== decompile " + f.getName() + " ====");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null)
                    w.println(r.getDecompiledFunction().getC());
                else w.println("(failed)");
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
