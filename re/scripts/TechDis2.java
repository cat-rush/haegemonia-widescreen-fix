import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TechDis2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\techdis2_report.txt"));
        Listing lst = currentProgram.getListing();
        Instruction inst = lst.getInstructionAt(toAddr(0x007d7d90L));
        while (inst != null && inst.getAddress().getOffset() <= 0x007d7dc0L) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
