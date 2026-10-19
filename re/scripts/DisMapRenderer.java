import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisMapRenderer extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dismap_report.txt"));
        Address a = toAddr(0x00739000L);
        Address end = toAddr(0x0073a400L);
        java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
        while (it.hasNext()) {
            Instruction ins = it.next();
            if (ins.getAddress().compareTo(end) > 0) break;
            w.println(ins.getAddress() + "  " + ins);
        }
        w.close();
        println("done");
    }
}
