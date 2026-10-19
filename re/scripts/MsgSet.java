import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class MsgSet extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgset_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] fns = {0x004c1020L, 0x004c1048L, 0x004c26a0L, 0x004c2d40L};
        for (long t : fns) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            if (it != null) {
                while (it.hasNext()) {
                    Reference r = it.next();
                    w.println("   from " + r.getFromAddress()
                        + (getFunctionContaining(r.getFromAddress()) != null
                           ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                             + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                }
            }
        }
        w.close();
        println("done");
    }
}
