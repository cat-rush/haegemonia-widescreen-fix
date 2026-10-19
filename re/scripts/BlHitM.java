import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitM extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitm_report.txt"));
        for (long a = 0x00713508L; a < 0x00713580L; ) {
            Instruction ins = getInstructionAt(toAddr(a));
            if (ins == null) { a += 1; continue; }
            w.println(ins.getAddress() + "  " + ins.toString());
            a += ins.getLength();
        }
        w.println("==== refs to FUN_00713508 ====");
        Reference[] refs = getReferencesTo(toAddr(0x00713508L));
        for (Reference ref : refs) {
            Function cf = getFunctionContaining(ref.getFromAddress());
            String fname = cf == null ? "?" : cf.getName();
            w.println("   " + ref.getReferenceType() + " from " + ref.getFromAddress() + " in " + fname);
        }
        w.close();
        println("done");
    }
}
