import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSetView;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FullFnDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fullfn_report.txt"));
        Function f = getFunctionContaining(toAddr(0x780fa1L));
        if (f == null) { w.println("fn not found"); w.close(); return; }
        AddressSetView body = f.getBody();
        InstructionIterator it = currentProgram.getListing().getInstructions(body, true);
        while (it.hasNext() && !monitor.isCancelled()) {
            Instruction ins = it.next();
            w.println(ins);
        }
        w.close();
        println("done");
    }
}
