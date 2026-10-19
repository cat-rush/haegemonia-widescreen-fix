import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnDis52 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fn52_report.txt"));
        Memory mem = currentProgram.getMemory();
        long[][] ranges = {{0x52f0ffL, 0x52f3e7L}, {0x54e900L, 0x54ec00L}};
        for (long[] rr : ranges) {
            w.println("==== 0x" + Long.toHexString(rr[0]) + " .. 0x" + Long.toHexString(rr[1]));
            Instruction ins = getInstructionAt(toAddr(rr[0]));
            while (ins != null && ins.getAddress().getOffset() <= rr[1]) {
                byte[] b = new byte[ins.getLength()];
                mem.getBytes(ins.getAddress(), b);
                StringBuilder sb = new StringBuilder();
                for (byte x : b) sb.append(String.format("%02X ", x));
                while (sb.length() < 20) sb.append(' ');
                w.println(ins.getAddress() + "  " + sb.toString() + "|  " + ins);
                ins = getInstructionAfter(ins);
            }
        }
        w.close();
        println("done");
    }
}
