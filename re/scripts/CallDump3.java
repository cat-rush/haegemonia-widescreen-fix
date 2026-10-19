// CallDump3.java - callers of the text system entry FUN_004f0a57
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CallDump3 extends GhidraScript {

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
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\calldump3_report.txt"));
        callers(0x004f0a57L);   // text system entry
        callers(0x004f014eL);   // text layout
        callers(0x00504948L);   // icon draw path (menu icons?)
        callers(0x005311b4L);   // icon draw path
        callers(0x00435213L);   // icon draw path
        callers(0x0050db50L);
        callers(0x0050f465L);
        w.close();
        println("done: calldump3_report.txt");
    }
}
