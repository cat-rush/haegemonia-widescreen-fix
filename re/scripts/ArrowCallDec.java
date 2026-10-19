import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ArrowCallDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\arrowcall_report.txt"));
        long target = 0x562475cL;
        w.println("==== disasm 0x5624460 .. 0x5624A40 ====");
        AddressSet set = new AddressSet(toAddr(0x5624460L), toAddr(0x5624A40L));
        InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
        int n = 0;
        while (it.hasNext() && !monitor.isCancelled()) {
            Instruction ins = it.next();
            w.println(ins);
            if (++n > 800) break;
        }
        w.close();
        println("done");
    }
}
