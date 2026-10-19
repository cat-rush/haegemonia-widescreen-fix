import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class L12Body extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\l12body_report.txt"));
        Listing lst = currentProgram.getListing();
        Instruction inst = lst.getInstructionAt(toAddr(0x00712c5eL));
        while (inst != null && inst.getAddress().getOffset() <= 0x00712da6L) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
