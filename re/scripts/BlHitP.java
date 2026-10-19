import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHitP extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhitp_report.txt"));
        for (long a = 0x007133fcL; a < 0x0071347eL; ) {
            Instruction ins = getInstructionAt(toAddr(a));
            if (ins == null) { a += 1; continue; }
            w.println(ins.getAddress() + "  " + ins.toString());
            a += ins.getLength();
        }
        w.close();
        println("done");
    }
}
