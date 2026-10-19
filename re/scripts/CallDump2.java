// CallDump2.java - callers of the two text-path entry fns + their decompiles
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CallDump2 extends GhidraScript {

    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        w.println(dr.getDecompiledFunction().getC());
        w.flush();
        di.dispose();
    }

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
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\calldump2_report.txt"));
        callers(0x004f014eL);   // queue caller
        callers(0x0044f648L);   // efac caller
        w.println("== decompiles ==");
        dec(0x004f014eL);
        dec(0x0044f648L);
        w.close();
        println("done: calldump2_report.txt");
    }
}
