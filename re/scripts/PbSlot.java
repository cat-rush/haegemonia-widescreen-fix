import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PbSlot extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pbslot_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] fns = {0x00505c2aL, 0x005059ecL, 0x004ff370L};
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
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x00505c2aL));
        w.println("=== xrefs to FUN_00505c2a");
        if (it != null) {
            int c = 0;
            while (it.hasNext() && c < 30) {
                Reference rr = it.next();
                w.println("   from " + rr.getFromAddress()
                    + (getFunctionContaining(rr.getFromAddress()) != null
                       ? " fn=" + getFunctionContaining(rr.getFromAddress()).getName()
                         + " " + getFunctionContaining(rr.getFromAddress()).getBody() : ""));
                c++;
            }
        }
        w.close();
        println("done");
    }
}
