import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Dis4752 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dis4752_report.txt"));
        Memory mem = currentProgram.getMemory();
        long a = 0x47523aL;
        long end = 0x4754C0L;
        Instruction ins = getInstructionAt(toAddr(a));
        int n = 0;
        while (ins != null && ins.getAddress().getOffset() < end && n < 200) {
            byte[] b = new byte[ins.getLength()];
            mem.getBytes(ins.getAddress(), b);
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02X ", x));
            while (sb.length() < 20) sb.append(' ');
            w.println(ins.getAddress() + "  " + sb.toString() + "|  " + ins);
            ins = getInstructionAfter(ins);
            n++;
        }
        w.close();
        println("done");
    }
}
