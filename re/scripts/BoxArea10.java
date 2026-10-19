// BoxArea10.java - disasm around 0x4AAF88 (the ship world SetTransform call)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea10 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea10_report.txt"));
        Function f = getFunctionContaining(toAddr(0x4AAF88L));
        w.println("== function containing 0x4AAF88: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "?") + " ==");
        if (f != null) {
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  caller " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
        }
        Address a = toAddr(0x4AAF88L - 0x50);
        Address end = toAddr(0x4AAF88L + 0x30);
        while (a != null && a.getOffset() < end.getOffset()) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("  " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done: boxarea10_report.txt");
    }
}
