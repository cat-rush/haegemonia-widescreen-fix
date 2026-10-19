import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class Xrefs5 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefs5_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x00c2903cL, 0x00c29048L, 0x00c29033L};
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
