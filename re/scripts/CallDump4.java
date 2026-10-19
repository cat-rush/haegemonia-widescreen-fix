// CallDump4.java - callers of the text funnel FUN_004ef060
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CallDump4 extends GhidraScript {

    PrintWriter w;

    void callers(long va) throws Exception {
        w.println("== callers of " + Long.toHexString(va) + " ==");
        try {
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(toAddr(va));
            while (it.hasNext()) {
                Reference r = it.next();
                Function cf = getFunctionContaining(r.getFromAddress());
                w.println("   " + r.getReferenceType() + " from " + r.getFromAddress() +
                          " in " + (cf != null ? cf.getName() + " @ " + cf.getEntryPoint() : "?"));
            }
        } catch (Exception e) {
            w.println("   EXC: " + e.getMessage());
        }
        w.flush();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\calldump4_report.txt"));
        callers(0x004ef060L);   // text funnel (renders all text objects)
        callers(0x004f0a57L);
        w.close();
        println("done: calldump4_report.txt");
    }
}
