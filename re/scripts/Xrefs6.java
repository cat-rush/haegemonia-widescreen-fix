import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class Xrefs6 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\xrefs6_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] targets = {0x00c330e0L, 0x00c330e4L, 0x00c330f0L, 0x00c330f4L, 0x00c3310cL, 0x00c33114L};
        for (long t : targets) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 12) {
                    Reference r = it.next();
                    Function f = getFunctionContaining(r.getFromAddress());
                    w.println("   from " + r.getFromAddress() + " " + (f != null ? f.getName() + " " + f.getBody() : "?"));
                    c++;
                }
            }
        }
        w.close();
        println("done");
    }
}
