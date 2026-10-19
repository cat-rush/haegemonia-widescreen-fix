import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Prologue4ef060 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\prologue4ef060_report.txt"));
        long[] addrs = {0x004ef060L, 0x00437bb4L};
        for (long a : addrs) {
            w.println("== bytes at " + Long.toHexString(a));
            for (int i = 0; i < 24; i++) {
                byte b = getByte(toAddr(a + i));
                w.print(String.format("%02X ", b & 0xff));
            }
            w.println();
        }
        w.close();
        println("done");
    }
}
