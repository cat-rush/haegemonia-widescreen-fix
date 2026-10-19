import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisForce extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\disforce_report.txt"));
        long[][] ranges = { {0x004e8c00L, 0x004e9400L}, {0x004ebc00L, 0x004ec300L} };
        for (long[] r : ranges) {
            w.println("===== range " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]) + " =====");
            Address a = toAddr(r[0]);
            Address end = toAddr(r[1]);
            disassemble(a);
            AddressSet set = new AddressSet(a, end);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(set, true);
            int n = 0;
            while (it.hasNext()) {
                Instruction ins = it.next();
                w.println(ins.getAddress() + "  " + ins);
                if (++n > 900) { w.println("... (truncated)"); break; }
            }
        }
        w.close();
        println("done");
    }
}
