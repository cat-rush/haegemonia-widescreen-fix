import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LerpXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\lerpxref_report.txt"));
        w.println("==== xrefs to FUN_0040c2a0");
        int n = 0;
        for (Reference ref : getReferencesTo(toAddr(0x0040c2a0L))) {
            Function f = getFunctionContaining(ref.getFromAddress());
            w.println("  " + ref.getFromAddress() + "  in " +
                      (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
            if (++n >= 120) { w.println("  ..."); break; }
        }
        w.println("total: " + n);
        w.close();
        println("done");
    }
}
