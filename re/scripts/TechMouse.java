import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class TechMouse extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\techmouse_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        long[] globs = {0xc68030L, 0xc68034L, 0xc68038L, 0xc6803cL, 0xb2e448L, 0xb3a848L};
        for (long t : globs) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            w.println("=== refs to 0x" + Long.toHexString(t) + " from tech range 0x7d4000..0x7df800");
            int c = 0;
            if (it != null) {
                while (it.hasNext()) {
                    Reference r = it.next();
                    long a = r.getFromAddress().getOffset();
                    if (a >= 0x7d4000L && a <= 0x7df800L) {
                        Function f = getFunctionContaining(r.getFromAddress());
                        w.println("   from " + r.getFromAddress()
                            + (f != null ? " fn=" + f.getName() + " " + f.getBody() : ""));
                        c++;
                    }
                }
            }
            w.println("   (in-range count " + c + ")");
        }
        w.close();
        println("done");
    }
}
