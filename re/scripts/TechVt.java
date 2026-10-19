import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class TechVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\techvt_report.txt"));
        w.println("=== vtable at 0xb3ae34 (TechTreeScreen?)");
        for (int i = 0; i < 60; i++) {
            Address a = toAddr(0xb3ae34L + i * 4);
            int v = getInt(a);
            if (v >= 0x400000 && v < 0x400000 + 0xA00000) {
                w.println(String.format("   +%02X -> 0x%08X%s", i * 4, v,
                    getFunctionContaining(toAddr(v & 0xFFFFFFFFL)) != null
                    ? " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getName()
                      + " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getBody() : ""));
            } else {
                w.println(String.format("   +%02X -> 0x%08X", i * 4, v));
            }
        }
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x007d8fb4L, 0x007db62bL, 0x007d51a0L, 0x007de159L, 0x007dcc7bL};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 40) {
                    Reference r = it.next();
                    w.println("   from " + r.getFromAddress()
                        + (getFunctionContaining(r.getFromAddress()) != null
                           ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                             + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                    c++;
                }
            }
        }
        w.close();
        println("done");
    }
}
