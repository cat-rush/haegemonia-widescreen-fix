import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Data;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/* Find the UI job strings (the panel renderer builds "_fjob_ w800 a center ..."
   jobs) and dump every reference to them + the surrounding code. */
public class JobStrXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\jobstr_report.txt"));
        MemoryBlock[] blocks = currentProgram.getMemory().getBlocks();
        int found = 0;
        for (MemoryBlock b : blocks) {
            if (!b.isInitialized()) continue;
            long start = b.getStart().getOffset();
            long end = b.getEnd().getOffset();
            for (long a = start; a < end && found < 40; a++) {
                // look for "center" and "_fjob_"
                byte[] buf = new byte[8];
                if (a + 8 > end) break;
                currentProgram.getMemory().getBytes(toAddr(a), buf);
                String s = new String(buf, "US-ASCII");
                if (s.toLowerCase().contains("center") || s.toLowerCase().contains("fjob")) {
                    // align backwards to the string start (ASCIIZ)
                    long s0 = a;
                    while (s0 > start) {
                        byte bb = currentProgram.getMemory().getByte(toAddr(s0 - 1));
                        int bbi = bb & 0xff;
                        if (bbi < 0x20 || bbi > 0x7e) break;
                        s0--;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (long p2 = s0; p2 < a + 16 && p2 < end; p2++) {
                        int ch = currentProgram.getMemory().getByte(toAddr(p2)) & 0xff;
                        if (ch == 0 || ch < 0x20 || ch > 0x7e) break;
                        sb.append((char) ch);
                    }
                    String full = sb.toString();
                    w.println("=== string @" + toAddr(s0) + ": \"" + full.replaceAll("[\\r\\n]", "?") + "\"");
                    ReferenceManager rm = currentProgram.getReferenceManager();
                    Iterator<Reference> it = rm.getReferencesTo(toAddr(s0));
                    if (it != null) {
                        int c = 0;
                        while (it.hasNext() && c < 12) {
                            Reference r = it.next();
                            w.println("   ref from " + r.getFromAddress() + " type=" + r.getReferenceType()
                                      + (getFunctionContaining(r.getFromAddress()) != null ? " fn=" + getFunctionContaining(r.getFromAddress()).getName() : ""));
                            c++;
                        }
                    }
                    found++;
                    a += 7;
                }
            }
        }
        w.println("total: " + found);
        w.close();
        println("done");
    }
}
