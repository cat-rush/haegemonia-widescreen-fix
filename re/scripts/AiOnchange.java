import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.CodeUnitIterator;
import ghidra.program.model.listing.CodeUnit;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiOnchange extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aionchange_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        w.println("===== VTABLE DUMP 0xb37f0c (popup?) =====");
        for (int i = 0; i < 80; i++) {
            long a = 0xb37f0cL + i * 4;
            int v = getInt(toAddr(a));
            Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
            w.println(String.format("  [%2d] 0x%08X -> %s", i, v,
                f != null ? f.getName() + " @ " + f.getEntryPoint() : "?"));
        }
        w.println("");

        w.println("===== VTABLE DUMP 0xb31e64 (row) =====");
        for (int i = 0; i < 80; i++) {
            long a = 0xb31e64L + i * 4;
            int v = getInt(toAddr(a));
            Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
            w.println(String.format("  [%2d] 0x%08X -> %s", i, v,
                f != null ? f.getName() + " @ " + f.getEntryPoint() : "?"));
        }
        w.println("");

        w.println("===== XREFS TO 0x7d30fc (count store) =====");
        for (Reference r : getReferencesTo(toAddr(0x7d30fcL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");

        w.println("===== XREFS TO 0x7befbe (row click) =====");
        for (Reference r : getReferencesTo(toAddr(0x7befbeL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");

        w.println("===== XREFS TO 0xb37f0c (popup vtable data refs) =====");
        for (Reference r : getReferencesTo(toAddr(0xb37f0cL))) {
            Function caller = getFunctionContaining(r.getFromAddress());
            w.println("  from " + r.getFromAddress() + " in " +
                (caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?"));
        }
        w.println("");

        w.println("===== INSTRUCTIONS WITH DISP 0x1cc (whole image) =====");
        Instruction inst = getFirstInstruction();
        int cnt = 0;
        while (inst != null && cnt < 6000) {
            long a = inst.getAddress().getOffset();
            if (a >= 0x400000L && a < 0xCE0000L) {
                String mnem = inst.getMnemonicString();
                if (!mnem.startsWith("J") && !mnem.startsWith("CALL")) {
                    byte[] b = null;
                    try { b = inst.getBytes(); } catch (Exception e) {}
                    if (b != null && b.length >= 4) {
                        for (int k = 0; k + 4 <= b.length; k++) {
                            long disp = (b[k] & 0xFFL) | ((b[k+1] & 0xFFL) << 8) |
                                         ((b[k+2] & 0xFFL) << 16) | ((b[k+3] & 0xFFL) << 24);
                            if (disp == 0x1ccL) {
                                Function f = getFunctionContaining(inst.getAddress());
                                w.println("  0x" + Long.toHexString(a) + " in " +
                                    (f != null ? f.getName() + " @ " + f.getEntryPoint() : "?") +
                                    " : " + inst.toString());
                                cnt++;
                                break;
                            }
                        }
                    }
                }
            }
            inst = inst.getNext();
        }
        w.println("  (count=" + cnt + ")");
        w.println("");

        long[] dec = { 0x7d30fcL, 0x7d1ef2L, 0x828ff4L, 0x5552bcL, 0x56cd8dL, 0x6c4250L, 0x51c8e0L, 0x7bf616L };
        for (long a : dec) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DECOMP 0x" + Long.toHexString(a) + " ==========");
            w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                DecompileResults res = di.decompileFunction(f, 180, monitor);
                if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
                else w.println("!!! decompile failed");
            }
            w.println("");
        }

        w.close();
        println("done");
    }
}
