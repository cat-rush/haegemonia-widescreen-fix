// CallDump.java - dump callers/containers for the text-shift discriminator work
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CallDump extends GhidraScript {

    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        w.println(dr.getDecompiledFunction().getC());
        w.flush();
        di.dispose();
    }

    void callers(long va) throws Exception {
        w.println("== callers of " + Long.toHexString(va) + " ==");
        try {
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(toAddr(va));
            while (it.hasNext()) {
                Reference r = it.next();
                Function cf = getFunctionContaining(r.getFromAddress());
                w.println("   " + r.getReferenceType() + " from " + r.getFromAddress() +
                          " in " + (cf != null ? cf.getName() + " @ " + cf.getEntryPoint() : "?"));
            }
        } catch (Exception e) {
            w.println("   EXC: " + e.getMessage());
        }
        w.flush();
    }

    void container(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println("   " + Long.toHexString(va) + " is inside " +
                  (f != null ? f.getName() + " @ " + f.getEntryPoint() : "?"));
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\calldump_report.txt"));
        callers(0x00438670L);   // text queue
        callers(0x0044efacL);   // scaled sprite/glyph builder
        callers(0x0044ecd6L);   // glyph builder
        w.println("== containers of draw call sites ==");
        container(0x0039037L);
        container(0x004EF9EL);
        container(0x00E8616L);
        container(0x00E929FL);
        container(0x00EBDBBL);
        container(0x01429E5L);
        container(0x00732B3L);
        container(0x025F08FL);
        container(0x0141EF1L);
        container(0x0041C8CL);
        container(0x03122C2L);
        w.println("== decompile 400.0f imm fns (selection clamp candidates) ==");
        dec(0x00418c2fL);
        dec(0x004e0b02L);
        dec(0x0068ee7eL);
        w.close();
        println("done: calldump_report.txt");
    }
}
