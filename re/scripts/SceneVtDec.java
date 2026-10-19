import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SceneVtDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\scenevt_report.txt"));
        long[] vts = {0xb31adcL, 0xb319d4L};
        for (long vt : vts) {
            w.println("==== vtable 0x" + Long.toHexString(vt));
            for (int i = 0; i < 28; i++) {
                long off = vt + i * 4;
                int v = 0;
                try { v = getInt(toAddr(off)); } catch (Exception e) { break; }
                Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
                w.println("  [" + i + "] 0x" + Integer.toHexString(v) +
                          (f != null ? " = " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            }
            Reference[] refs = getReferencesTo(toAddr(vt));
            for (Reference r : refs) {
                Address f = r.getFromAddress();
                Function fn = getFunctionContaining(f);
                w.println("   ref from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
            }
        }
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        // the two ctors we know + the update fns around them
        long[] fns = {0x52eb20L, 0x522f70L, 0x52f0ffL, 0x54f288L, 0x54f4eeL, 0x54f633L};
        for (long f : fns) {
            Function fn = getFunctionAt(toAddr(f));
            if (fn == null) { w.println("=== 0x" + Long.toHexString(f) + ": no function"); continue; }
            w.println("=== DECOMP 0x" + Long.toHexString(f) + " " + fn.getBody());
            DecompileResults r = ifc.decompileFunction(fn, 300, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("FAILED");
            }
        }
        w.close();
        println("done");
    }
}
