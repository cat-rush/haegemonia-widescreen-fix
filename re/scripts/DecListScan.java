import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class DecListScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\declistscan_report.txt"));
        long[] strAddrs = {0x00c28f64L, 0x00c28f78L, 0x00c28f80L};
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (long sa : strAddrs) {
            Iterator<Reference> it = rm.getReferencesTo(toAddr(sa));
            int c = 0;
            while (it.hasNext()) {
                Reference r = it.next();
                Address from = r.getFromAddress();
                Function f = getFunctionContaining(from);
                w.println("==== str 0x" + Long.toHexString(sa) + " xref " + from + " fn " + (f != null ? f.getName() + " " + f.getBody() : "?"));
                if (f != null) {
                    DecompileResults res = ifc.decompileFunction(f, 120, monitor);
                    if (res != null && res.decompileCompleted()) {
                        w.println(res.getDecompiledFunction().getC());
                    } else {
                        w.println("decompile failed");
                    }
                }
                c++;
                if (c > 4) break;
            }
        }
        w.close();
        println("done");
    }
}
