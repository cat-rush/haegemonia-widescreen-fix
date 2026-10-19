import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisDrawHelpers extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\disdraw_report.txt"));
        Address a = toAddr(0x004473b7L);
        Address end = toAddr(0x00447800L);
        java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
        while (it.hasNext()) {
            Instruction ins = it.next();
            if (ins.getAddress().compareTo(end) > 0) break;
            w.println(ins.getAddress() + "  " + ins);
        }
        w.println("== xrefs to 0x4473b7 ==");
        ghidra.program.model.symbol.Reference[] r1 = getReferencesTo(toAddr(0x004473b7L));
        for (int i = 0; i < r1.length; i++) w.println("  from " + r1[i].getFromAddress());
        w.println("== xrefs to 0x4473ef ==");
        ghidra.program.model.symbol.Reference[] r2 = getReferencesTo(toAddr(0x004473efL));
        for (int i = 0; i < r2.length; i++) w.println("  from " + r2[i].getFromAddress());
        w.println("== xrefs to 0x447337 ==");
        ghidra.program.model.symbol.Reference[] r3 = getReferencesTo(toAddr(0x00447337L));
        for (int i = 0; i < r3.length; i++) w.println("  from " + r3[i].getFromAddress());
        w.close();
        println("done");
    }
}
