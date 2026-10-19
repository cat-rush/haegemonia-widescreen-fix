import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Prologue extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\prologue_report.txt"));
        long[] addrs = { 0x006c3b4dL };
        for (long a : addrs) {
            Address addr = toAddr(a);
            byte[] bytes = new byte[24];
            currentProgram.getMemory().getBytes(addr, bytes);
            w.print(Long.toHexString(a) + ": ");
            for (byte b : bytes) w.print(String.format("%02X ", b));
            w.println();
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(addr, true);
            int n = 0;
            while (it.hasNext() && n < 6) {
                Instruction ins = it.next();
                w.println("  " + ins.getAddress() + "  " + ins);
                n++;
            }
        }
        w.close();
        println("done");
    }
}
