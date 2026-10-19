import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SceneWAsm extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\scenewasm_report.txt"));
        AddressSet set = new AddressSet(toAddr(0x52f0ffL), toAddr(0x52f1a0L));
        InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
        int n = 0;
        while (it.hasNext() && !monitor.isCancelled()) {
            Instruction ins = it.next();
            w.println(ins);
            if (++n > 80) break;
        }
        w.close();
        println("done");
    }
}
