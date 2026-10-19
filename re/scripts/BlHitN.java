import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitN extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitn_report.txt"));
        long[] fns = {0x004a04fbL, 0x004a89f3L, 0x004a0119L};
        for (long a : fns) {
            w.println("==== refs to " + Long.toHexString(a) + " ====");
            Reference[] refs = getReferencesTo(toAddr(a));
            for (Reference ref : refs) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                String fname = cf == null ? "?" : cf.getName() + " [" + cf.getEntryPoint() + ".." + cf.getBody().getMaxAddress() + "]";
                w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
            }
        }
        w.close();
        println("done");
    }
}
