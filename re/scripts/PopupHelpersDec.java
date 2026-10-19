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

public class PopupHelpersDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popuphelpers_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x00525cd9L, 0x00529e3aL, 0x00529d60L, 0x0052a760L,
                         0x00529ca2L, 0x00529c29L, 0x0055155dL, 0x0053d4a4L};
        for (long s : starts) {
            Function f = getFunctionContaining(toAddr(s));
            if (f == null) { w.println("=== no fn at 0x" + Long.toHexString(s)); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = ifc.decompileFunction(f, 300, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("DECOMPILE FAILED");
            }
        }
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] datas = {0x00b31a54L, 0x00b31a50L, 0x00b316d4L, 0x00b316d0L};
        String[] names = {"b31a54(790)", "b31a50(590)", "b316d4(800=layoutW)", "b316d0(600)"};
        for (int i = 0; i < datas.length; i++) {
            w.println("");
            w.println("==== xrefs to " + names[i] + " @ 0x" + Long.toHexString(datas[i]));
            Iterator<Reference> it = rm.getReferencesTo(toAddr(datas[i]));
            int c = 0;
            while (it != null && it.hasNext() && c < 40) {
                Reference rr = it.next();
                Function fn = getFunctionContaining(rr.getFromAddress());
                w.println("   from " + rr.getFromAddress()
                    + (fn != null ? " fn=" + fn.getName() + " @ " + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
                c++;
            }
        }
        w.close();
        println("done");
    }
}
