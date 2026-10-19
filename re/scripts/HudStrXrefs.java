import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class HudStrXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\hudstrxref_report.txt"));
        long[] targets = {
            0x00c286e0L, 0x00c28730L, 0x00c2d974L, 0x00c2d9c4L,
            0x00c286c0L, 0x00c286d0L, 0x00c2d954L, 0x00c2d964L,
            0x00c285acL, 0x00c286a4L
        };
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 60) {
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
