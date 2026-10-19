import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MeshObst extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\meshobst_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0048cf81L));
        Instruction inst = getInstructionAt(f.getEntryPoint());
        boolean found = false;
        while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0) {
            String s = inst.toString();
            if (s.contains("0x70") || s.contains("[EDX + 0x70]") || found) {
                w.println(inst.getAddress() + ": " + s);
                found = true;
                if (inst.getAddress().getOffset() > 0x0048dc00L) break;
            }
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
