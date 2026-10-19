import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ProjFnDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\projfn_report.txt"));
        AddressSet set = new AddressSet(toAddr(0x6495e0L), toAddr(0x6497e3L));
        InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
        while (it.hasNext() && !monitor.isCancelled()) {
            Instruction ins = it.next();
            w.println("  " + ins);
        }
        w.close();
        println("done");
    }
}
