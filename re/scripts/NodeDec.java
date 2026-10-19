// NodeDec.java - decompile the scene node update + screen-model transform
// chain (the container computation). FUN_00746e40 (node update called from
// FUN_0047523a), FUN_0052f0ff (scene widget draw), FUN_004b6ffc (screen
// model renderer), FUN_004aaeed + the model transform getter targets.
// Report -> nodedec_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class NodeDec extends GhidraScript {
    void dump(PrintWriter w, DecompInterface d, long a, int maxlines) {
        Function f = getFunctionContaining(toAddr(a));
        if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); return; }
        w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
        Reference[] refs = getReferencesTo(f.getEntryPoint());
        w.println("callers:");
        for (Reference ref : refs) {
            if (ref.getFromAddress().getOffset() < 0x400000L) continue;
            Function cf = getFunctionContaining(ref.getFromAddress());
            w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
        }
        DecompileResults r = d.decompileFunction(f, 120, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            String c = r.getDecompiledFunction().getC();
            String[] lines = c.split("\n");
            int n = Math.min(lines.length, maxlines);
            for (int i = 0; i < n; i++) w.println(lines[i]);
            if (lines.length > n) w.println("   ... (truncated)");
        } else w.println("(decompile failed)");
    }
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\nodedec_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x746e40, 0x52f0ff, 0x4b6ffc, 0x4aaeed, 0x474d66, 0x4e8e0f, 0x4ba82b };
        for (long a : fns) dump(w, d, a, 80);
        d.dispose();
        w.close();
        println("done: nodedec_report.txt");
    }
}
