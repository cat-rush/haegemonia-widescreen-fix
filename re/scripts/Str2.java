import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Str2 extends GhidraScript {
    PrintWriter w;
    void str(long va) throws Exception {
        Memory mem = currentProgram.getMemory();
        StringBuilder sb = new StringBuilder();
        long a = va;
        for (int i = 0; i < 100; i++) {
            try {
                byte b = mem.getByte(toAddr(a));
                if (b == 0) break;
                if (b >= 32 && b < 127) sb.append((char) b);
                else sb.append('.');
            } catch (Exception e) { break; }
            a++;
        }
        w.println("0x" + Long.toHexString(va) + ": [" + sb.toString() + "]");
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\str2_report.txt"));
        str(0x00c06854L);
        str(0x00c06864L);
        str(0x00c06862L);
        str(0x00c068b9L);
        w.close();
        println("done");
    }
}
