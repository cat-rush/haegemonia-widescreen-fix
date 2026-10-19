import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class F002Asm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\f002asm_report.txt"));
        w.println("==== disasm 0x54f002..0x54f060");
        AddressSet set = new AddressSet(toAddr(0x54f002L), toAddr(0x54f060L));
        InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
        int n = 0;
        while (it.hasNext() && !monitor.isCancelled()) {
            Instruction ins = it.next();
            w.println(ins);
            if (++n > 60) break;
        }
        w.close();
        println("done");
    }
}
