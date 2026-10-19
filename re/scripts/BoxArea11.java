// BoxArea11.java - disasm FUN_004aaeed entry forward
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea11 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea11_report.txt"));
        Address a = toAddr(0x4aaeedL);
        int n = 0;
        while (n < 220) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) { w.println("  <no instruction at " + a + ">"); break; }
            w.println("  " + ins);
            a = ins.getAddress().add(ins.getLength());
            n++;
            if (n > 3 && ins.toString().startsWith("RET")) break;
        }
        w.println("== callers of FUN_004b6ffc ==");
        try {
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(toAddr(0x4b6ffcL));
            for (ghidra.program.model.symbol.Reference ref : refs) {
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
        } catch (Exception e) { w.println("  EXC " + e.getMessage()); }
        w.close();
        println("done: boxarea11_report.txt");
    }
}
