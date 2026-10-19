import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Fn13126Dis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("fn13126dis_report.txt"));
        ghidra.program.model.listing.Function f = getFunctionAt(toAddr(0x713126L));
        if (f == null) { w.println("no fn"); w.close(); return; }
        Instruction inst = getInstructionAt(f.getEntryPoint());
        while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0) {
            w.println(inst.getAddress() + ": " + inst);
            inst = inst.getNext();
        }
        w.close();
    }
}
