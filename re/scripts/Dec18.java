import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec18 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec18_report.txt"));
        long vt = 0x00b3aeb0L;
        w.println("======== strip vtable PTR_FUN_00b3aeb0 entries");
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] slots = new long[16];
        int n = 0;
        for (int i = 0; i < 16; i++) {
            int v = getInt(toAddr(vt + (long)i * 4));
            if (v == 0) break;
            slots[n++] = v & 0xffffffffL;
            w.println("  [" + i + "] = 0x" + Long.toHexString(v & 0xffffffffL));
        }
        for (int i = 0; i < n; i++) {
            long a = slots[i];
            Function f = getFunctionAt(toAddr(a));
            w.println("==================== DEC strip method [" + i + "] @ " + Long.toHexString(a));
            if (f == null) { w.println("NO FUNCTION"); continue; }
            w.println("name: " + f.getName() + "  body: " + f.getBody());
            DecompileResults r = ifc.decompileFunction(f, 120, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("decompile failed");
            }
        }
        w.close();
        println("done");
    }
}
