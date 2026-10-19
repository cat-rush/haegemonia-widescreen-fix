import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class L12Callers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\l12callers_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x00712c5eL));
        w.println("=== xrefs to FUN_00712c5e");
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
