import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PbDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pbdec_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x0054a8d0L};
        for (long f : fns) {
            Address a = toAddr(f);
            w.println("=== DECOMP 0x" + Long.toHexString(f));
            DecompileResults r = ifc.decompileFunction(getFunctionAt(a), 300, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("FAILED");
            }
        }
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x0054a8d0L));
        w.println("=== xrefs to FUN_0054a8d0 (ProgressBar ctor?)");
        if (it != null) {
            int c = 0;
            while (it.hasNext() && c < 40) {
                Reference r = it.next();
                w.println("   from " + r.getFromAddress()
                    + (getFunctionContaining(r.getFromAddress()) != null
                       ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                         + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                c++;
            }
        }
        w.close();
        println("done");
    }
}
