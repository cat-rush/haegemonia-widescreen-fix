import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.mem.MemoryBlock;
import java.io.FileWriter;
import java.io.PrintWriter;

public class StrBolygo extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bolygo_report.txt"));
        MemoryBlock[] blocks = currentProgram.getMemory().getBlocks();
        byte[] needle = "bolygo".getBytes("ASCII");
        for (MemoryBlock b : blocks) {
            if (!b.isInitialized()) continue;
            long start = b.getStart().getOffset();
            long end = b.getEnd().getOffset();
            byte[] data = new byte[(int)(end - start + 1)];
            b.getBytes(b.getStart(), data);
            for (int i = 0; i + needle.length <= data.length; i++) {
                boolean ok = true;
                for (int j = 0; j < needle.length; j++) {
                    if (data[i + j] != needle[j]) { ok = false; break; }
                }
                if (ok) {
                    long va = start + i;
                    w.println("string 'bolygo' at " + Long.toHexString(va));
                    Reference[] refs = getReferencesTo(toAddr(va));
                    for (Reference r : refs) {
                        Function f = getFunctionContaining(r.getFromAddress());
                        w.println("   xref from " + r.getFromAddress() +
                                  (f != null ? " fn=" + f.getName() + " [" + f.getEntryPoint() + ".." + f.getBody().getMaxAddress() + "]" : ""));
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
