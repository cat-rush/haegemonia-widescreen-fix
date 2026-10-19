import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class PbXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pbxref_report.txt"));
        long[] targets = {0x00c0beaeL, 0x00c0cb94L, 0x00c0b45cL, 0x00c0b468L};
        ReferenceManager rm = currentProgram.getReferenceManager();
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
