import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class IconDis2 extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\icondis2_report.txt"));
        Address a = toAddr(0x0044ECD6L);
        while (a.getOffset() < 0x0044F660L) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            String s = ins.toString();
            if (s.contains("4728e") || (a.getOffset() >= 0x0044EF80 && a.getOffset() <= 0x0044EFB0) ||
                (a.getOffset() >= 0x0044F230 && a.getOffset() <= 0x0044F2E1)) {
                w.println(a + "  " + ins);
            }
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done");
    }
}
