import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PopupCtorDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popupctor_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x0052388fL, 0x0054e90fL, 0x0054a8d0L, 0x0053bfdaL, 0x00525bf5L};
        for (long s : starts) {
            Address a = toAddr(s);
            Function f = getFunctionContaining(a);
            if (f == null) { w.println("=== no fn at 0x" + Long.toHexString(s)); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = ifc.decompileFunction(f, 300, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("DECOMPILE FAILED");
            }
        }
        w.println("");
        w.println("==== xrefs to FUN_0052388f (popup ctor)");
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x0052388fL));
        int c = 0;
        while (it.hasNext() && c < 60) {
            Reference rr = it.next();
            Function fn = getFunctionContaining(rr.getFromAddress());
            w.println("   from " + rr.getFromAddress()
                + (fn != null ? " fn=" + fn.getName() + " @ " + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
            c++;
        }
        w.println("==== xrefs to FUN_00525d66 (popup clamp)");
        it = rm.getReferencesTo(toAddr(0x00525d66L));
        c = 0;
        while (it.hasNext() && c < 60) {
            Reference rr = it.next();
            Function fn = getFunctionContaining(rr.getFromAddress());
            w.println("   from " + rr.getFromAddress()
                + (fn != null ? " fn=" + fn.getName() + " @ " + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
            c++;
        }
        w.println("==== xrefs to FUN_00553274 (tooltip opener)");
        it = rm.getReferencesTo(toAddr(0x00553274L));
        c = 0;
        while (it.hasNext() && c < 60) {
            Reference rr = it.next();
            Function fn = getFunctionContaining(rr.getFromAddress());
            w.println("   from " + rr.getFromAddress()
                + (fn != null ? " fn=" + fn.getName() + " @ " + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
            c++;
        }
        w.close();
        println("done");
    }
}
