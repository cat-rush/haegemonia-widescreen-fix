import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.mem.MemoryBlock;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GridXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\gridxref_report.txt"));
        long[] fns = {0x0077ed6aL, 0x0077eddeL, 0x0077ee70L};
        for (long a : fns) {
            w.println("==== xrefs to 0x" + Long.toHexString(a));
            int n = 0;
            for (Reference ref : getReferencesTo(toAddr(a))) {
                Function f = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + "  in " +
                          (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
                if (++n >= 60) { w.println("  ..."); break; }
            }
            if (n == 0) w.println("  none");
        }
        w.println("==== scan for imm 0x75300 (480000) / 0x12c00 (76800) in .text");
        MemoryBlock[] blocks = currentProgram.getMemory().getBlocks();
        for (MemoryBlock b : blocks) {
            if (!b.isExecute()) continue;
            long len = b.getSize();
            for (long off = 0; off + 4 < len; off++) {
                if (monitor.isCancelled()) break;
                byte[] t = new byte[4];
                if (b.getBytes(b.getStart().add(off), t) != 4) continue;
                int v = (t[0] & 0xff) | ((t[1] & 0xff) << 8) | ((t[2] & 0xff) << 16) | ((t[3] & 0xff) << 24);
                if (v == 0x75300 || v == 480000 || v == 0x12c00 || v == 76800) {
                    w.println("  imm at " + b.getStart().add(off) + " = 0x" + Integer.toHexString(v));
                }
            }
        }
        w.close();
        println("done");
    }
}
