import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class HudDis2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\huddis_report.txt"));
        Address a = toAddr(0x0077EFC8L);
        int n = 0;
        while (n < 400) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            if (a.getOffset() >= 0x77F790L && a.getOffset() < 0x77F950L) {
                w.println("   " + ins.getAddress() + "  " + ins);
            }
            a = ins.getAddress().add(ins.getLength());
            n++;
        }
        w.close();
        println("done");
    }
}
