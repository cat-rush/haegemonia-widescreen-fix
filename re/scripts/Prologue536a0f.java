import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Prologue536a0f extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\prologue536a0f_report.txt"));
        long a = 0x00536a0fL;
        w.println("== bytes at " + Long.toHexString(a));
        for (int i = 0; i < 32; i++) {
            byte b = getByte(toAddr(a + i));
            w.print(String.format("%02X ", b & 0xff));
        }
        w.println();
        w.close();
        println("done");
    }
}
