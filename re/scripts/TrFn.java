import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class TrFn extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\trfn_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Address a = toAddr(0x0077ae14L);
        w.println("=== DECOMP 0x77ae14 (TR panel fn)");
        DecompileResults r = ifc.decompileFunction(getFunctionAt(a), 300, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("FAILED");
        }
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x0047352fL, 0x00447337L, 0x004473b7L};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t) + " from TR fn 0x77ae14..0x77ec1f");
            if (it != null) {
                while (it.hasNext()) {
                    Reference rr = it.next();
                    long a2 = rr.getFromAddress().getOffset();
                    if (a2 >= 0x77ae14L && a2 <= 0x77ec1fL) {
                        w.println("   from " + rr.getFromAddress());
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
