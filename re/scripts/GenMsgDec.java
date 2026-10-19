import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GenMsgDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\genmsg_report.txt"));

        // 1. resolve class name of a vtable via COL->TypeDescriptor->name
        // 2. list xrefs to a vtable
        long[] vts = {0xb3ab9cL, 0xb31d14L};
        for (long vt : vts) {
            w.println("==== vtable 0x" + Long.toHexString(vt) + " class=" + className(vt));
            Reference[] refs = getReferencesTo(toAddr(vt));
            for (Reference r : refs) {
                Address f = r.getFromAddress();
                Function fn = getFunctionContaining(f);
                w.println("   ref from " + f + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : ""));
            }
        }

        // 3. RTTI walk for cGeneralMessages (0xc2841c)
        w.println("==== RTTI walk for cGeneralMessages @ 0xc2841c");
        long rtti = 0xc2841cL;
        Reference[] refs = getReferencesTo(toAddr(rtti));
        for (Reference r : refs) {
            w.println("   ref to RTTI string from " + r.getFromAddress());
        }
        long col = rtti - 4;
        w.println("   COL candidate @ 0x" + Long.toHexString(col) + " = 0x" + Integer.toHexString(getInt(toAddr(col))));
        Reference[] colrefs = getReferencesTo(toAddr(col));
        for (Reference r : colrefs) {
            w.println("   ref to COL from " + r.getFromAddress());
        }
        long col4 = rtti - 8;
        Reference[] col4refs = getReferencesTo(toAddr(col4));
        for (Reference r : col4refs) {
            w.println("   ref to (rtti-8) from " + r.getFromAddress());
        }

        // 4. decompile candidate draw fns
        long[] fns = {
            0x7261d0L, 0x727620L, 0x726700L, 0x726990L, 0x726ac0L, 0x726d80L, 0x727040L, 0x7271f0L, 0x7273a0L,
            0x71d214L, 0x71fc4aL, 0x72194bL, 0x7235baL, 0x788e0aL, 0x803520L
        };
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
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

    String className(long vt) throws Exception {
        int col = getInt(toAddr(vt - 4));
        if (col <= 0 || col > 0xffffffffL) return "?";
        // COL: u32 sig, u32 offset, u32 cdOffset, u32 pTypeDescriptor
        int td = 0;
        try { td = getInt(toAddr((col & 0xffffffffL) + 12)); } catch (Exception e) { return "?col"; }
        if (td <= 0) return "?td";
        try {
            int namep = getInt(toAddr((td & 0xffffffffL) + 8));
            if (namep <= 0) return "?name";
            byte[] b = new byte[100];
            int n = 0;
            try {
                for (int i = 0; i < 100; i++) {
                    byte c = getByte(toAddr((namep & 0xffffffffL) + i));
                    if (c == 0) break;
                    b[n++] = c;
                }
            } catch (Exception e) {}
            return new String(b, 0, n, "ASCII");
        } catch (Exception e) { return "?td2"; }
    }
}
