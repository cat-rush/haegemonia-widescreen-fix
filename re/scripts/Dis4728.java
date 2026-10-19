import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dis4728 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dis4728_report.txt"));
        long[][] ranges = { {0x004471f6L, 0x004473b6L}, {0x004e9290L, 0x004e92a5L} };
        for (long[] r : ranges) {
            w.println("===== " + Long.toHexString(r[0]) + " .. " + Long.toHexString(r[1]) + " =====");
            Address a = toAddr(r[0]);
            Address end = toAddr(r[1]);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
            while (it.hasNext()) {
                Instruction ins = it.next();
                if (ins.getAddress().compareTo(end) > 0) break;
                w.println(ins.getAddress() + "  " + ins);
            }
        }
        w.close();
        println("done");
    }
}
