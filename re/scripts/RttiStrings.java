import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RttiStrings extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rttistrings_report.txt"));
        long[] addrs = {0x00b9953cL, 0x00b99588L, 0x00b995d4L, 0x00b994f8L};
        for (long a : addrs) {
            StringBuilder sb = new StringBuilder();
            long cur = a;
            while (cur < a + 128) {
                byte b = getByte(toAddr(cur));
                if (b == 0) break;
                sb.append((char)(b & 0xff));
                cur++;
            }
            w.println(Long.toHexString(a) + " -> '" + sb.toString() + "'");
        }
        w.close();
        println("done");
    }
}
