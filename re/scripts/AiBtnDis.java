import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiBtnDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aibtndis_report.txt"));
        long[] sites = {0x007b0e7bL, 0x007b37faL};
        for (long r : sites) {
            w.println("==== disasm window around " + Long.toHexString(r) + " ====");
            Instruction inst = currentProgram.getListing().getInstructionAt(toAddr(r - 128));
            while (inst != null && inst.getAddress().compareTo(toAddr(r + 256)) <= 0) {
                w.println(inst.getAddress().toString() + "  " + inst.toString());
                inst = currentProgram.getListing().getInstructionAfter(inst.getAddress());
            }
        }
        w.close();
        println("done");
    }
}
