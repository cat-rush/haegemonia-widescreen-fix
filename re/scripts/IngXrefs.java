import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class IngXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ingxref_report.txt"));
        long[] targets = {
            0x00c151acL, 0x00c151b0L, 0x00c151b4L, 0x00c151b8L,
            0x00c151bcL, 0x00c151c0L, 0x00c151c4L, 0x00c15294L,
            0x00c14760L, 0x00c14768L, 0x00c14770L, 0x00c14778L,
            0x00c1478cL, 0x00c1c70cL, 0x00c1c714L, 0x00c1c71cL,
            0x00c1c724L, 0x00c1361aL, 0x00c1311cL, 0x00c13134L
        };
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
