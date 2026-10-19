// FloatXref.java - xref the ship-position rdata floats + decompile the
// owners (the container/panel construction code). Report -> floatxref_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class FloatXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\floatxref_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0xbc4516, 0xb998a6, 0xba2357, 0xba2397, 0xbfc3de };
        Set<Function> fns = new HashSet<Function>();
        for (long a : addrs) {
            w.println("==== refs to " + Long.toHexString(a) + " ====");
            Reference[] refs = getReferencesTo(toAddr(a));
            for (Reference ref : refs) {
                w.println("  " + ref.getFromAddress());
                Function cf = getFunctionContaining(ref.getFromAddress());
                if (cf != null) {
                    w.println("      in " + cf.getName() + " (" + cf.getEntryPoint() + ")");
                    fns.add(cf);
                }
            }
        }
        w.println();
        for (Function f : fns) {
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                String c = r.getDecompiledFunction().getC();
                String[] lines = c.split("\n");
                int n = Math.min(lines.length, 60);
                for (int i = 0; i < n; i++) w.println(lines[i]);
                if (lines.length > n) w.println("   ... (truncated)");
            } else w.println("(decompile failed)");
        }
        d.dispose();
        w.close();
        println("done: floatxref_report.txt");
    }
}
