import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DecPlanetNames extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\planetnames_report.txt"));
        long[] addrs = {0x00516877L, 0x0084f61bL, 0x007a8730L};
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(Long.toHexString(a) + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 60, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 60000) c = c.substring(0, 60000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
            if (a == 0x0084f61bL) {
                w.println("---- callers of " + f.getName() + " ----");
                Reference[] refs = getReferencesTo(f.getEntryPoint());
                for (Reference ref : refs) {
                    if (ref.getReferenceType().isCall()) {
                        w.println("   from " + ref.getFromAddress());
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
