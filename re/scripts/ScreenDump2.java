// ScreenDump2.java - lightweight: sizes, callers, first disasm of frame fns
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ScreenDump2 extends GhidraScript {
    PrintWriter w;

    void head(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " +
                  (f != null ? f.getEntryPoint() : toAddr(va)) +
                  "  size=" + (f != null ? f.getBody().getNumAddresses() : -1) + " ----");
        Address a = toAddr(va);
        for (int i = 0; i < 50; i++) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }

    void callers(long va) throws Exception {
        w.println("== callers of " + Long.toHexString(va) + " ==");
        try {
            ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(toAddr(va));
            int n = 0;
            while (it.hasNext() && n < 30) {
                Reference r = it.next();
                Function cf = getFunctionContaining(r.getFromAddress());
                w.println("   " + r.getReferenceType() + " from " + r.getFromAddress() +
                          " in " + (cf != null ? cf.getName() + " @ " + cf.getEntryPoint() : "?"));
                n++;
            }
        } catch (Exception e) { w.println("   EXC: " + e.getMessage()); }
        w.flush();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\screen2_report.txt"));
        head(0x00690c9bL);
        head(0x0069e24eL);
        head(0x00521246L);
        callers(0x0069e24eL);
        callers(0x00690c9bL);
        w.close();
        println("done: screen2_report.txt");
    }
}
