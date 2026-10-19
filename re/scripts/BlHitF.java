import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitF extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitf_report.txt"));
        for (long a = 0x0079a2c0L; a < 0x0079a480L; ) {
            Instruction ins = getInstructionAt(toAddr(a));
            if (ins == null) { a += 1; continue; }
            w.println(ins.getAddress() + "  " + ins.toString());
            a += ins.getLength();
        }
        w.close();
        println("done");
    }
}
