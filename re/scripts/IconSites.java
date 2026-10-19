import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class IconSites extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\iconsites_report.txt"));
        Address a = toAddr(0x0077ae14L);
        Address end = toAddr(0x0077d5a3L);
        int n = 0;
        while (a.compareTo(end) < 0) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            String s = ins.toString();
            if (s.contains("CALL 0x0044f648") || s.contains("CALL 0x0044efac") || s.contains("CALL 0x0044ecd6")) {
                w.println(ins.getAddress() + ": " + s);
                n++;
            }
            a = ins.getAddress().add(ins.getLength());
        }
        w.println("total icon calls in FUN_0077ae14: " + n);
        w.close();
        println("done");
    }
}
