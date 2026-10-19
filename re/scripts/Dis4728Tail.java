import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dis4728Tail extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dis4728tail_report.txt"));
        Address a = toAddr(0x00447290L);
        Address end = toAddr(0x00447400L);
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
