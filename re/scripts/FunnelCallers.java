import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class FunnelCallers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\funnelcallers_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x004ef060L));
        int c = 0;
        if (it != null) {
            while (it.hasNext()) {
                Reference r = it.next();
                w.println("   from " + r.getFromAddress()
                    + (getFunctionContaining(r.getFromAddress()) != null
                       ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                         + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                c++;
            }
        }
        w.println("total=" + c);
        w.close();
        println("done " + c);
    }
}
