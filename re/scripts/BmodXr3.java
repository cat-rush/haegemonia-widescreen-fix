import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BmodXr3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bmodxr_report.txt"));
        String[] tags = { "MESH", "VERT", "ISTR", "TRPS", "OBST", "MBOX", "BLST" };
        long startOff = 0x400000L;
        long endOff = 0x400000L + 0xA00000L;
        for (String tag : tags) {
            w.println("==== tag '" + tag + "' ====");
            byte[] pat = tag.getBytes("ASCII");
            int shown = 0;
            for (long off = startOff; off < endOff - pat.length && shown <= 30; off++) {
                boolean ok = true;
                for (int i = 0; i < pat.length; i++) {
                    byte b = 0;
                    try { b = getByte(toAddr(off + i)); } catch (Exception e) { ok = false; break; }
                    if (b != pat[i]) { ok = false; break; }
                }
                if (!ok) continue;
                Address a = toAddr(off);
                shown++;
                w.println("  data at " + a);
                Reference[] refs = getReferencesTo(a);
                for (Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("    ref from " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + " .. " + cf.getBody().getMaxAddress() + ")" : ""));
                }
                off += pat.length - 1;
            }
        }
        w.close();
        println("done");
    }
}
