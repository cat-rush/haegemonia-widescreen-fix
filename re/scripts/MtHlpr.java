import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class MtHlpr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mthlpr_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        w.println("=== DECOMP 0x71092a (FUN_0071092a)");
        DecompileResults r = ifc.decompileFunction(getFunctionAt(toAddr(0x0071092aL)), 300, monitor);
        if (r != null && r.getDecompiledFunction() != null)
            w.println(r.getDecompiledFunction().getC());
        else w.println("FAILED");
        w.println("=== DECOMP 0x710ddd (FUN_00710ddd)");
        r = ifc.decompileFunction(getFunctionAt(toAddr(0x00710dddL)), 300, monitor);
        if (r != null && r.getDecompiledFunction() != null)
            w.println(r.getDecompiledFunction().getC());
        else w.println("FAILED");
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x0071092aL, 0x00710dddL};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 40) {
                    Reference rr = it.next();
                    w.println("   from " + rr.getFromAddress()
                        + (getFunctionContaining(rr.getFromAddress()) != null
                           ? " fn=" + getFunctionContaining(rr.getFromAddress()).getName()
                             + " " + getFunctionContaining(rr.getFromAddress()).getBody() : ""));
                    c++;
                }
            }
        }
        w.close();
        println("done");
    }
}
