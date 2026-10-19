import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dec17 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dec17_report.txt"));
        // who references PTR_FUN_00b3aeb0 (the strip vtable)
        long vt = 0x00b3aeb0L;
        w.println("======== xrefs to strip vtable " + Long.toHexString(vt));
        for (Reference ref : getReferencesTo(toAddr(vt))) {
            Function f = getFunctionContaining(ref.getFromAddress());
            w.println("  " + ref.getFromAddress() + " in " + (f != null ? f.getName() : "?"));
        }
        // xrefs to FUN_0056c8fb (add-widget) - find its callers
        w.println("======== xrefs to FUN_0056c8fb");
        Function fb = getFunctionAt(toAddr(0x0056c8fbL));
        if (fb != null) {
            for (Reference ref : getReferencesTo(fb.getEntryPoint())) {
                Function f = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + " in " + (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
            }
        }
        // decompile the widget draw fn fully (FUN_005400cf) - big but try
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        Function f = getFunctionAt(toAddr(0x005400cfL));
        w.println("======== DEC FUN_005400cf");
        if (f != null) {
            DecompileResults r = ifc.decompileFunction(f, 180, monitor);
            if (r != null && r.decompileCompleted()) {
                w.println(r.getDecompiledFunction().getC());
            } else w.println("decompile failed");
        }
        w.close();
        println("done");
    }
}
