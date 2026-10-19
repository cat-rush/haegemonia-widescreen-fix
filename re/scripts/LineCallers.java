import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class LineCallers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\linecallers_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] fns = {0x00711c0aL, 0x00711da4L, 0x00712755L};
        for (long t : fns) {
            Function f = getFunctionAt(toAddr(t));
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t) + (f != null ? " (" + f.getName() + ")" : ""));
            if (it != null) {
                int c = 0;
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
