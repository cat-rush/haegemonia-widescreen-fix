import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class Xrefs4 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefs4_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x007fb480L};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 40) {
                    Reference r = it.next();
                    w.println("   from " + r.getFromAddress() + " " + (getFunctionContaining(r.getFromAddress()) != null ? getFunctionContaining(r.getFromAddress()).getName() + " " + getFunctionContaining(r.getFromAddress()).getBody() : "?"));
                    c++;
                }
            }
        }
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x007fb480L));
        if (f != null) {
            DecompileResults res = ifc.decompileFunction(f, 60, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            }
        }
        w.close();
        println("done");
    }
}
