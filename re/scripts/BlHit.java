import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.RefType;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);

        long[] mglob = {0x00c68030L, 0x00c68034L};
        String[] mnames = {"c68030 mouseX", "c68034 mouseY"};
        for (int gi = 0; gi < 2; gi++) {
            Address ga = toAddr(mglob[gi]);
            Reference[] refs = getReferencesTo(ga);
            w.println("==== xrefs to " + mnames[gi] + " (from 0x760000..0x7B0000) ====");
            for (Reference ref : refs) {
                long fa = ref.getFromAddress().getOffset();
                if (fa < 0x760000L || fa > 0x7B0000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName() + " [" + cf.getEntryPoint() + ".." + cf.getBody().getMaxAddress() + "]";
                w.println("   from " + ref.getFromAddress() + " type=" + ref.getReferenceType() + " fn=" + fname);
            }
        }

        long[] fns = {0x0077efc8L, 0x00780fa1L};
        for (long a : fns) {
            Function f = getFunctionAt(toAddr(a));
            if (f == null) f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println(a + " -> NO FN"); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " ====");
            DecompileResults r = di.decompileFunction(f, 90, monitor);
            if (r != null && r.decompileCompleted()) {
                String c = r.getDecompiledFunction().getC();
                if (c.length() > 120000) c = c.substring(0, 120000) + "\n... TRUNCATED ...";
                w.println(c);
            } else {
                w.println("DECOMP FAILED");
            }
        }
        w.close();
        println("done");
    }
}
