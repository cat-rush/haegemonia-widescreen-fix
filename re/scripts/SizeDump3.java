import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SizeDump3 extends GhidraScript {
    PrintWriter w;
    void fn(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println(Long.toHexString(va) + " in " + (f != null ? f.getName() + " @ " + f.getEntryPoint() + " size=" + f.getBody().getNumAddresses() : "?"));
        if (f != null) {
            Function n = getFunctionAfter(f.getEntryPoint());
            w.println("   next: " + (n != null ? n.getName() + " @ " + n.getEntryPoint() : "?"));
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(f.getEntryPoint());
            int c = 0;
            while (it.hasNext() && c < 25) {
                Reference r = it.next();
                Function cf = getFunctionContaining(r.getFromAddress());
                w.println("   caller: " + r.getFromAddress() + " in " + (cf != null ? cf.getName() : "?"));
                c++;
            }
        }
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\size3_report.txt"));
        fn(0x004E8616L);
        fn(0x00541EF1L);
        fn(0x00540691L);
        w.close();
        println("done");
    }
}
