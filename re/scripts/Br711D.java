import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Br711D extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\br711d_report.txt"));
        ghidra.program.model.address.Address start = toAddr(0x00711c00L);
        ghidra.program.model.address.Address end = toAddr(0x00711db0L);
        ghidra.program.model.listing.Instruction inst = getInstructionContaining(start);
        while (inst != null && inst.getAddress().compareTo(end) < 0) {
            Function f = getFunctionContaining(inst.getAddress());
            String fn = f != null ? "  [fn " + f.getName() + "]" : "";
            w.println(inst.getAddress() + ": " + inst.toString() + fn);
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
