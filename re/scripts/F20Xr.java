import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class F20Xr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\f20xr_report.txt"));
        Function f = getFunctionContaining(toAddr(0x00499f20L));
        w.println("FUN_00499f20 callers:");
        Reference[] refs = getReferencesTo(f.getEntryPoint());
        for (Reference ref : refs) {
            if (ref.getFromAddress().getOffset() < 0x400000) continue;
            Function cf = getFunctionContaining(ref.getFromAddress());
            w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + " .. " + cf.getBody().getMaxAddress() + ")" : ""));
        }
        w.close();
        println("done");
    }
}
