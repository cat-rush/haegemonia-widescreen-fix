import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TechDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\techdis_report.txt"));
        Listing lst = currentProgram.getListing();
        Instruction inst = lst.getInstructionAt(toAddr(0x007d7cb5L));
        while (inst != null && inst.getAddress().getOffset() <= 0x007d7d20L) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
        }
        w.println("=== disasm FUN_009e15bc (start)");
        inst = lst.getInstructionAt(toAddr(0x009e15bcL));
        int n = 0;
        while (inst != null && n < 12) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
            n++;
        }
        w.close();
        println("done");
    }
}
