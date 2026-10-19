import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ecd6Pro extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ecd6pro_report.txt"));
        Address a = toAddr(0x0044ECD6L);
        for (int i = 0; i < 12; i++) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + a + "  " + ins + "   (len " + ins.getLength() + ")");
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done");
    }
}
