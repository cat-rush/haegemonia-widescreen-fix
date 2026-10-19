import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSetView;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiFillDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aifilldis_report.txt"));
        long lo = 0x007bd9f0L, hi = 0x007bdc60L;
        Instruction inst = currentProgram.getListing().getInstructionAt(toAddr(lo));
        while (inst != null && inst.getAddress().compareTo(toAddr(hi)) <= 0) {
            w.println(inst.getAddress().toString() + "  " + inst.toString());
            inst = currentProgram.getListing().getInstructionAfter(inst.getAddress());
        }
        w.close();
        println("done");
    }
}
