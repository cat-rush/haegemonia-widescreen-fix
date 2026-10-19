import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MboxDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mboxdis_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0048be3dL));
        Instruction inst = getInstructionAt(f.getEntryPoint());
        while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0) {
            String s = inst.toString();
            if (s.contains("CALL")) {
                w.println(inst.getAddress() + ": " + s);
            }
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
