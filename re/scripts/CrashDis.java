import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CrashDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\crashdis_report.txt"));
        Listing lst = currentProgram.getListing();
        w.println("=== around crash addr 0x58A9BF");
        Instruction inst = lst.getInstructionAt(toAddr(0x0058a970L));
        int n = 0;
        while (inst != null && inst.getAddress().getOffset() <= 0x0058a9d0L) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
        }
        w.println("=== FUN_009e15bc full disasm (first 80)");
        inst = lst.getInstructionAt(toAddr(0x009e15bcL));
        n = 0;
        while (inst != null && n < 80) {
            w.println(inst.getAddress() + "  " + inst);
            inst = inst.getNext();
            n++;
        }
        w.close();
        println("done");
    }
}
