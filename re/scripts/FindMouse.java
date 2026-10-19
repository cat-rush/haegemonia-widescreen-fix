import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FindMouse extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\findmouse_report.txt"));
        AddressSet set = new AddressSet(toAddr(0x00500000L), toAddr(0x00530000L));
        for (Function f : currentProgram.getFunctionManager().getFunctions(set, true)) {
            boolean hit = false;
            for (Instruction ins : currentProgram.getListing().getInstructions(f.getBody(), true)) {
                String s = ins.toString();
                if (s.contains("0xc68030") || s.contains("0xc68034")) {
                    hit = true;
                    w.println("   " + ins.getAddress() + "  " + s);
                }
            }
            if (hit) {
                w.println("FUNCTION " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            }
        }
        w.close();
        println("done");
    }
}
