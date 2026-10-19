import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DwPtr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dwptr_report.txt"));
        long[] addrs = { 0x009e15dcL, 0x009e4f30L, 0x009e16ccL, 0x009e328cL, 0x00813dc4L };
        for (long a : addrs) {
            w.println("==== data 0x" + Long.toHexString(a) + " ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(a));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  ref from " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + " .. " + cf.getBody().getMaxAddress() + ")" : " (no fn)"));
            }
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x0042c380L };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println("==== " + f.getName() + " ====");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null)
                    w.println(r.getDecompiledFunction().getC());
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
