import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtCall extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtcall_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0073B65DL));
        for (Instruction ins : currentProgram.getListing().getInstructions(f.getBody(), true)) {
            String s = ins.toString();
            if (s.contains("0x18(") || s.contains("0x1c(") || s.contains("0x20(") || s.contains("0x24(")) {
                w.println("   " + ins.getAddress() + "  " + s);
            }
        }
        w.close();
        println("done");
    }
}
