import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/* Xrefs to the layout constants (layoutW 0xb316d4, layoutCenter 0xb3a848,
   the neighbor 0xb3a84c used by the strip renderer) - find the code that
   computes the panel container centering. Dump each ref + containing fn. */
public class LayoutXrefs2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\layoutxref2_report.txt"));
        long[] targets = {0x00b316d4L, 0x00b3a848L, 0x00b3a84cL, 0x00b316d0L, 0x00b2e448L};
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (long t : targets) {
            w.println("=== xrefs to 0x" + Long.toHexString(t));
            Iterator<Reference> it = rm.getReferencesTo(toAddr(t));
            int c = 0;
            if (it != null) {
                while (it.hasNext() && c < 60) {
                    Reference r = it.next();
                    Function fn = getFunctionContaining(r.getFromAddress());
                    w.println("   from " + r.getFromAddress() + " type=" + r.getReferenceType()
                              + (fn != null ? " fn=" + fn.getName() + " " + fn.getBody() : " ?"));
                    c++;
                }
            }
            w.println();
        }
        w.close();
        println("done");
    }
}
