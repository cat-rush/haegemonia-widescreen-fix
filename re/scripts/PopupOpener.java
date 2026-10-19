import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PopupOpener extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popupopener_report.txt"));
        long t = 0x00525d66L;
        w.println("==== xrefs to FUN_00525d66");
        for (Reference ref : getReferencesTo(toAddr(t))) {
            Function f = getFunctionContaining(ref.getFromAddress());
            w.println("  " + ref.getFromAddress() + "  in " +
                      (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
        }
        /* decompile each caller */
        java.util.Set<Long> done = new java.util.HashSet<Long>();
        for (Reference ref : getReferencesTo(toAddr(t))) {
            Function f = getFunctionContaining(ref.getFromAddress());
            if (f == null || done.contains(f.getEntryPoint().getOffset())) continue;
            done.add(f.getEntryPoint().getOffset());
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompInterface ifc = new DecompInterface();
            ifc.openProgram(currentProgram);
            DecompileResults res = ifc.decompileFunction(f, 300, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("DECOMPILE FAILED");
            }
            ifc.dispose();
        }
        w.close();
        println("done");
    }
}
