import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PbCtor extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pbctor_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Address a = toAddr(0x0052ac80L);
        w.println("=== DECOMP 0x52ac80 (ProgressBar ctor)");
        DecompileResults r = ifc.decompileFunction(getFunctionAt(a), 300, monitor);
        if (r != null && r.getDecompiledFunction() != null) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("FAILED");
        }
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(a);
        w.println("=== xrefs to FUN_0052ac80");
        if (it != null) {
            int c = 0;
            while (it.hasNext() && c < 40) {
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
