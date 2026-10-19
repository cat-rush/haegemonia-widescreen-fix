import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.util.task.TaskMonitor;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ObjCull extends GhidraScript {
    void dec(DecompInterface d, long va, PrintWriter w, String tag) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) {
            w.println("== " + tag + " 0x" + Long.toHexString(va) + ": NO FUNCTION ==");
            return;
        }
        w.println("==== " + tag + " " + f.getName() + " (" + f.getEntryPoint() + " .. " +
                  f.getBody().getMaxAddress() + ") ====");
        DecompileResults r = d.decompileFunction(f, 120, monitor);
        if (r != null && r.decompileCompleted()) {
            w.println(r.getDecompiledFunction().getC());
        } else {
            w.println("  (decompile failed)");
        }
    }
    void xrefs(long va, PrintWriter w, String tag) throws Exception {
        Address a = toAddr(va);
        w.println("== refs to " + tag + " " + a + " ==");
        int n = 0;
        for (Reference ref : getReferencesTo(a)) {
            w.println("   from " + ref.getFromAddress());
            Function f = getFunctionContaining(ref.getFromAddress());
            if (f != null) w.println("     fn " + f.getName());
            if (++n > 60) { w.println("   ..."); break; }
        }
        if (n == 0) w.println("   (none)");
    }
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\objcull_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        /* the cull hunt: panel culled when baked OBST x crosses 800 design */
        long[] e800 = {0x6c3cc5L, 0x6c3d1eL, 0x6c9ec2L, 0x6dee70L, 0x6deea1L,
                       0x714982L, 0x7176adL, 0x780303L, 0x780987L, 0x785fa3L,
                       0x7a5f5bL, 0x7a5fbdL, 0xbf6fbeL};
        for (long a : e800) xrefs(a, w, "800.0f");
        /* decompile the OBST / BMOD loader candidates */
        dec(d, 0x0048cc8aL, w, "OBST thunk");
        dec(d, 0x0048cf81L, w, "MESH parser");
        /* the runtime-rewritten screenselect panel block on disk */
        w.println("== functions overlapping 0x42c380..0x42c600 ==");
        for (Function f : currentProgram.getFunctionManager().getFunctions(toAddr(0x42c380L), true)) {
            if (f.getEntryPoint().getOffset() > 0x42c600L) break;
            w.println("   " + f.getName() + " @ " + f.getEntryPoint() + " body " + f.getBody());
        }
        dec(d, 0x42c4a10L, w, "panel 42c4a10");
        dec(d, 0x00712985L, w, "fn 712985");
        dec(d, 0x0071176adL > 0 ? 0x717500L : 0, w, "fn 717500");
        /* where do the OBST positions get used: scan functions touching 800*scale */
        dec(d, 0x006de76fL, w, "fn 6de76f");
        w.close();
        d.dispose();
        println("done");
    }
}
