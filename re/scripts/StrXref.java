import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class StrXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\strxref_report.txt"));
        String[] needles = {"Gametext not found", "Item ", "not found int HashMap"};
        Memory mem = currentProgram.getMemory();
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (String needle : needles) {
            Address a = mem.getMinAddress();
            byte[] pat = needle.getBytes("ASCII");
            w.println("=== \"" + needle + "\"");
            int n = 0;
            while (a != null && n < 20) {
                a = mem.findBytes(a, pat, null, true, monitor);
                if (a == null) break;
                w.println("  at " + a);
                Iterator<Reference> it = rm.getReferencesTo(a);
                int c = 0;
                if (it != null) {
                    while (it.hasNext() && c < 10) {
                        Reference r = it.next();
                        w.println("     from " + r.getFromAddress() + " " + (getFunctionContaining(r.getFromAddress()) != null ? getFunctionContaining(r.getFromAddress()).getName() + " " + getFunctionContaining(r.getFromAddress()).getBody() : "?"));
                        c++;
                    }
                }
                a = a.add(1);
                n++;
            }
        }
        w.close();
        println("done");
    }
}
