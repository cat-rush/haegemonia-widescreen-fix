import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Data;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class MsgBoxHunt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxhunt_report.txt"));
        String[] keys = {
            "BriefMessage", "Messages1", "message.bmod", "cMessageScreen",
            "GeneralMessagePos", "GeneralMessageSize", "MESSAGE",
            "messages.inf", "messageinitials.inf", "cGeneralMessages"
        };
        ReferenceManager rm = currentProgram.getReferenceManager();
        java.util.Set<Long> done = new java.util.HashSet<Long>();
        Data it = getFirstData();
        while (it != null && !monitor.isCancelled()) {
            Object v = it.getValue();
            if (v instanceof String) {
                String s = (String) v;
                for (String k : keys) {
                    if (s.contains(k) && s.length() < 120) {
                        Address a = it.getAddress();
                        if (done.add(a.getOffset())) {
                            w.println("=== string \"" + s + "\" @ " + a);
                            Iterator<Reference> refs = rm.getReferencesTo(a);
                            int c = 0;
                            while (refs != null && refs.hasNext() && c < 80) {
                                Reference r = refs.next();
                                w.println("   from " + r.getFromAddress()
                                    + (getFunctionContaining(r.getFromAddress()) != null
                                       ? " fn=" + getFunctionContaining(r.getFromAddress()).getName()
                                         + " " + getFunctionContaining(r.getFromAddress()).getBody() : ""));
                                c++;
                            }
                        }
                    }
                }
            }
            it = getDataAfter(it);
        }
        w.close();
        println("done");
    }
}
