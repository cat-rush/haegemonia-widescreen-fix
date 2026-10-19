import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class BarLine extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\barline_report.txt"));
        Function f = getFunctionContaining(toAddr(0x00712927L));
        w.println("=== 0x712927 in fn: " + (f != null ? f.getName() + " " + f.getBody() : "?"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x00712927L));
        w.println("=== xrefs to 0x712927");
        if (it != null) {
            int c = 0;
            while (it.hasNext() && c < 40) {
                Reference r = it.next();
                w.println("   from " + r.getFromAddress()
                    + (getFunctionContaining(r.getFromAddress()) != null
                       ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                         + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                c++;
            }
        }
        if (f != null) {
            Iterator<Reference> it2 = rm.getReferencesTo(f.getEntryPoint());
            w.println("=== xrefs to " + f.getName());
            if (it2 != null) {
                int c = 0;
                while (it2.hasNext() && c < 40) {
                    Reference r = it2.next();
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
