import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PopupBytes extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popupbytes_report.txt"));
        long[] addrs = {0x00525d66L, 0x00526ce8L, 0x00526d19L, 0x00525bf5L, 0x00525e9eL, 0x00525cd9L};
        for (long a : addrs) {
            w.println("==== bytes at 0x" + Long.toHexString(a));
            for (int i = 0; i < 32; i++) {
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
