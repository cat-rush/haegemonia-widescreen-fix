import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BmodVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bmodvt_report.txt"));
        ghidra.program.model.mem.Memory mem = currentProgram.getMemory();
        /* find the vtable: a data table whose entries = FUN_0048b7e2 / FUN_0048cf81 / FUN_0048f6d6 */
        long startOff = 0xB2F000L;
        long endOff = 0xB30000L;
        int vt = -1;
        for (long off = startOff; off < endOff; off += 4) {
            int v = 0;
            try { v = getInt(toAddr(off)); } catch (Exception e) { continue; }
            if (v == 0x0048cf81 || v == 0x0048b7e2 || v == 0x0048f6d6) {
                vt = (int)off;
                break;
            }
        }
        w.println("vtable-ish found at 0x" + Long.toHexString(vt));
        if (vt > 0) {
            for (int i = 0; i < 40; i++) {
                long off = vt - (0x0048cf81 == getIntAtSafe(vt) ? 0 : 0);
                off = vt + i * 4;
                int v = 0;
                try { v = getInt(toAddr(off)); } catch (Exception e) { break; }
                Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
                w.println("  [" + i + "] 0x" + Integer.toHexString(v) + (f != null ? " = " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            }
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        if (vt > 0) {
            int v70 = 0;
            try { v70 = getInt(toAddr(vt + 0x70L)); } catch (Exception e) {}
            if (v70 > 0x400000) {
                Function f = getFunctionContaining(toAddr(v70 & 0xFFFFFFFFL));
                if (f != null) {
                    w.println("==== OBST handler " + f.getName() + " ====");
                    DecompileResults r = d.decompileFunction(f, 120, monitor);
                    if (r != null && r.getDecompiledFunction() != null)
                        w.println(r.getDecompiledFunction().getC());
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
    private int getIntAtSafe(long off) {
        try { return getInt(toAddr(off)); } catch (Exception e) { return 0; }
    }
}
