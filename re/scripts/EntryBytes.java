import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class EntryBytes extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\entrybytes_report.txt"));
        long[] addrs = {0x52f0ffL, 0x47523aL, 0x474d66L, 0x504948L};
        Memory mem = currentProgram.getMemory();
        for (long a : addrs) {
            w.println("==== 0x" + Long.toHexString(a));
            Instruction ins = getInstructionAt(toAddr(a));
            for (int i = 0; i < 10 && ins != null; i++) {
                byte[] b = new byte[ins.getLength()];
                mem.getBytes(ins.getAddress(), b);
                StringBuilder sb = new StringBuilder();
                for (byte x : b) sb.append(String.format("%02X ", x));
                w.println(ins.getAddress() + "  " + sb.toString().trim() + "  |  " + ins);
                ins = getInstructionAfter(ins);
            }
        }
        w.close();
        println("done");
    }
}
