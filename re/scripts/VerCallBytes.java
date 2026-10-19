import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VerCallBytes extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vercall_report.txt"));
        long[] addrs = {0x007d4320L, 0x007d42f0L, 0x005ac1c3L};
        for (long a : addrs) {
            w.println("==== bytes at 0x" + Long.toHexString(a));
            for (int i = 0; i < 64; i++) {
                byte b = getByte(toAddr(a + i));
                w.print(String.format("%02X ", b & 0xff));
                if (i % 16 == 15) w.println();
            }
            w.println();
        }
        w.close();
        println("done");
    }
}
