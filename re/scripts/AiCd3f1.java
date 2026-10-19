import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiCd3f1 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aicd3f1_report.txt"));
        Reference[] refs = getReferencesTo(toAddr(0x007cd3f1L));
        for (Reference r : refs) {
            Address from = r.getFromAddress();
            Function f = getFunctionContaining(from);
            w.println("  from " + from + (f != null ? " in " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            if (f != null) {
                DecompInterface di = new DecompInterface();
                di.openProgram(currentProgram);
                DecompileResults res = di.decompileFunction(f, 240, monitor);
                if (res != null && res.decompileCompleted()) {
                    w.println("========== DECOMP " + f.getName() + " ==========");
                    w.println(res.getDecompiledFunction().getC());
                }
            }
        }
        w.close();
        println("done");
    }
}
