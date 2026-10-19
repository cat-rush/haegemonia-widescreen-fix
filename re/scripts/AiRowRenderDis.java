import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiRowRenderDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\airowrender_report.txt"));
        long start = 0x7ce000L, end = 0x7cec60L;
        Instruction ins = getInstructionAt(toAddr(start));
        while (ins != null && ins.getAddress().getOffset() < end) {
            w.println("  " + ins.getAddress().toString() + "  " + ins.toString());
            ins = ins.getNext();
        }
        w.close();
        println("done");
    }
}
