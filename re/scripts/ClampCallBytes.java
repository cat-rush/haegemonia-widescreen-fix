import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ClampCallBytes extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\clampcall_report.txt"));
        long[] addrs = {0x00525f00L, 0x00525f18L, 0x00526d0eL};
        for (long a : addrs) {
            w.println("==== bytes at 0x" + Long.toHexString(a));
            for (int i = 0; i < 48; i++) {
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
