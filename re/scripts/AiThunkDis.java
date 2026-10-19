import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiThunkDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aithunk_report.txt"));
        long lo = 0x00ae3680L, hi = 0x00ae3780L;
        Instruction inst = currentProgram.getListing().getInstructionAt(toAddr(lo));
        while (inst != null && inst.getAddress().compareTo(toAddr(hi)) <= 0) {
            w.println(inst.getAddress().toString() + "  " + inst.toString());
            inst = currentProgram.getListing().getInstructionAfter(inst.getAddress());
        }
        w.close();
        println("done");
    }
}
