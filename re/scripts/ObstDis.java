import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ObstDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\obstdis_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0048e825L));
        ghidra.program.model.listing.Instruction inst = getInstructionAt(f.getEntryPoint());
        while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0) {
            w.println(inst.getAddress() + ": " + inst.toString());
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
