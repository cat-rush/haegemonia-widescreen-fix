import ghidra.app.script.GhidraScript;
import ghidra.program.model.data.StringDataType;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiStrDump extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aistrdump_report.txt"));
        Memory mem = currentProgram.getMemory();
        long[] addrs = { 0xc31158L, 0xc31164L, 0xc31174L, 0xc31180L, 0xc3118cL, 0xc31198L,
                         0xc311a4L, 0xc311b0L, 0xc311c8L, 0xc311e0L, 0xc311f8L, 0xc31210L,
                         0xc31224L, 0xc31238L, 0xc3124cL, 0xc31260L, 0xc31274L, 0xc30c58L,
                         0xc31428L, 0xc31434L, 0xc3143cL, 0xc31450L, 0xc31458L };
        for (long a : addrs) {
            byte[] buf = new byte[128];
            int got = mem.getBytes(toAddr(a), buf);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < got; i++) {
                byte b = buf[i];
                if (b == 0) break;
                sb.append(b >= 32 && b < 127 ? (char) b : '?');
            }
            w.println(String.format("0x%08X: \"%s\"", a, sb.toString()));
        }
        w.close();
        println("done");
    }
}
