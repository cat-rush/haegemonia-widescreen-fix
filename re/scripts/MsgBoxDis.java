import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgBoxDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxdis_report.txt"));
        Address a = toAddr(0x54aa60L);
        Address end = toAddr(0x54aac0L);
        while (a.compareTo(end) < 0) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) { w.println(a + ": (no instr)"); a = a.add(1); continue; }
            w.println(ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done");
    }
}
