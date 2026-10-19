import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BmodXr2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bmodxr_report.txt"));
        String[] tags = { "MESH", "VERT", "ISTR", "TRPS", "OBST", "MBOX", "BLST" };
        Memory mem = currentProgram.getMemory();
        for (String tag : tags) {
            w.println("==== tag '" + tag + "' ====");
            byte[] pat = tag.getBytes("ASCII");
            Address start = toAddr(0x400000L);
            ghidra.program.model.address.AddressSetView set = mem.findBytes(start, toAddr(0x420000L + 0x980000L), pat, null, true, monitor);
            int shown = 0;
            ghidra.program.model.address.AddressIterator it = set.getAddresses(true);
            while (it.hasNext() && shown <= 40) {
                Address a = it.next();
                shown++;
                w.println("  data at " + a);
                Reference[] refs = getReferencesTo(a);
                for (Reference ref : refs) {
                    if (ref.getFromAddress().getOffset() < 0x400000) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("    ref from " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + " .. " + cf.getBody().getMaxAddress() + ")" : ""));
                }
            }
        }
        w.close();
        println("done");
    }
}
