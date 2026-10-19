import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtableRange extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtablerange_report.txt"));
        long startVA = 0x00b318c0L;
        long endVA = 0x00b31980L;
        MemoryBlock b = getMemoryBlock(toAddr(startVA));
        byte[] data = getBytes(toAddr(startVA), (int)(endVA - startVA + 1));
        w.println("== vtable region dump (dwords) ==");
        for (int i = 0; i + 4 <= data.length; i += 4) {
            long va = startVA + i;
            long val = (data[i] & 0xffL) | ((data[i+1] & 0xffL) << 8) | ((data[i+2] & 0xffL) << 16) | ((data[i+3] & 0xffL) << 24);
            String tag = "";
            if (val >= 0x400000 && val <= 0x900000) {
                Function f = getFunctionContaining(toAddr(val));
                tag = f != null ? " -> " + f.getName() : " -> (code)";
            }
            w.println("  " + Long.toHexString(va) + " = " + Long.toHexString(val) + tag);
        }
        w.println("== xrefs into the vtable region ==");
        for (long a = startVA; a <= endVA; a += 4) {
            Reference[] refs = getReferencesTo(toAddr(a));
            for (Reference r : refs) {
                w.println("  ref to " + Long.toHexString(a) + " from " + r.getFromAddress());
            }
        }
        w.close();
        println("done");
    }
}
