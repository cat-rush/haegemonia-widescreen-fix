import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitT extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitt_report.txt"));
        for (long a = 0x00551ca5L; a < 0x00551d40L; ) {
            Instruction ins = getInstructionAt(toAddr(a));
            if (ins == null) { a += 1; continue; }
            w.println(ins.getAddress() + "  " + ins.toString());
            a += ins.getLength();
        }
        w.close();
        println("done");
    }
}
