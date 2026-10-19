import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class Xrefs7 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefs7_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x007fc315L, 0x007fc51fL, 0x007fc8c0L, 0x007fb679L};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 40) {
                    Reference r = it.next();
                    w.println("   from " + r.getFromAddress() + " " + (getFunctionContaining(r.getFromAddress()) != null ? getFunctionContaining(r.getFromAddress()).getName() + " " + getFunctionContaining(r.getFromAddress()).getBody() : "?"));
                    c++;
                }
            }
        }
        w.close();
        println("done");
    }
}
