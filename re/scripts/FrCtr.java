import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class FrCtr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\frctr_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0xc51278L));
        w.println("=== xrefs to 0xc51278");
        if (it != null) {
            while (it.hasNext()) {
                Reference r = it.next();
                w.println("   from " + r.getFromAddress()
                    + (getFunctionContaining(r.getFromAddress()) != null
                       ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                         + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
            }
        }
        w.close();
        println("done");
    }
}
