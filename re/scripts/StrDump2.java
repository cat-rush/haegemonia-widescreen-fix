import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

/* Dump the exact panel job strings (bytes) at the known addresses. */
public class StrDump2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\strdump2_report.txt"));
        long[] addrs = {0x00c286e0L, 0x00c28730L, 0x00c2d5ccL, 0x00c2d614L, 0x00c2d684L,
                        0x00c2d6c4L, 0x00c2d6d8L, 0x00c2d7b0L, 0x00c2d7d4L, 0x00c2d7fcL,
                        0x00c2d83cL, 0x00c2d870L, 0x00c2d9c4L, 0x00c2d974L};
        for (long a : addrs) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 48; i++) {
                int ch = currentProgram.getMemory().getByte(toAddr(a + i)) & 0xff;
                if (ch == 0) break;
                sb.append((char) (ch < 0x20 || ch > 0x7e ? '?' : ch));
            }
            w.println("0x" + Long.toHexString(a) + ": \"" + sb.toString() + "\"");
        }
        w.close();
        println("done");
    }
}
