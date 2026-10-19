import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtCall3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtcall3_report.txt"));
        AddressSet set = new AddressSet(toAddr(0x00400000L), toAddr(0x00B00000L));
        for (Function f : currentProgram.getFunctionManager().getFunctions(set, true)) {
            for (Instruction ins : currentProgram.getListing().getInstructions(f.getBody(), true)) {
                String s = ins.toString();
                if (s.startsWith("CALL") && (s.contains("0x18]") || s.contains("0x1c]"))) {
                    w.println("   " + ins.getAddress() + "  " + s + "   [in " + f.getName() + "]");
                }
            }
        }
        w.close();
        println("done");
    }
}
