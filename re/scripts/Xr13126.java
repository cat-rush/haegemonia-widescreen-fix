import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Xr13126 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("xr13126_report.txt"));
        ghidra.program.model.listing.Function f = getFunctionAt(toAddr(0x713126L));
        if (f == null) { w.println("no fn"); w.close(); return; }
        ghidra.program.model.symbol.ReferenceIterator it =
            currentProgram.getReferenceManager().getReferencesTo(f.getEntryPoint());
        for (ghidra.program.model.symbol.Reference ref : it) {
            ghidra.program.model.listing.Function caller = getFunctionContaining(ref.getFromAddress());
            w.println("ref from " + ref.getFromAddress() + " fn=" +
                (caller != null ? caller.getName() + " " + caller.getEntryPoint() : "?"));
        }
        w.close();
    }
}
