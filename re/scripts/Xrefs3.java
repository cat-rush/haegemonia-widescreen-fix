import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class Xrefs3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefs3_report.txt"));
        long[] targets = {0x007fb48fL, 0x007e82afL, 0x007e80beL};
        ReferenceManager rm = currentProgram.getReferenceManager();
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
