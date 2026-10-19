import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.scalar.Scalar;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SquadPos extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\squadpos_report.txt"));
        ghidra.program.model.listing.Listing listing = currentProgram.getListing();
        Address min = toAddr(0x00400000L);
        Address max = toAddr(0x00960000L);
        ghidra.program.disassemble.Disassembler dis =
            ghidra.program.disassemble.Disassembler.getDisassembler(currentProgram, monitor, null);
        ghidra.program.model.address.AddressSetView asv = new ghidra.program.model.address.AddressSet(min, max);
        dis.disassemble(min, asv, true);
        java.util.Iterator<Instruction> it = listing.getInstructions(min, true);
        int n = 0;
        while (it.hasNext() && n < 60000) {
            Instruction ins = it.next();
            if (ins.getAddress().compareTo(max) > 0) break;
            if (ins.toString().contains("0x7644")) {
                Function f = getFunctionContaining(ins.getAddress());
                w.println(ins.getAddress() + "  " + ins + (f != null ? "   in " + f.getName() + " .. " + f.getBody().getMaxAddress() : ""));
                if (++n >= 400) break;
            }
        }
        w.close();
        println("done " + n);
    }
}
