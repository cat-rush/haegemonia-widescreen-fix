import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai7d2d extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai7d2d_report.txt"));
        long lo = 0x007d2a00L, hi = 0x007d3600L;
        Instruction inst = currentProgram.getListing().getInstructionAt(toAddr(lo));
        while (inst != null && inst.getAddress().compareTo(toAddr(hi)) <= 0) {
            w.println(inst.getAddress().toString() + "  " + inst.toString());
            inst = currentProgram.getListing().getInstructionAfter(inst.getAddress());
        }
        w.close();
        println("done");
    }
}
