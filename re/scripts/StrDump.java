// StrDump.java - dump strings at given addresses
import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class StrDump extends GhidraScript {
    PrintWriter w;
    void str(long va) throws Exception {
        Memory mem = currentProgram.getMemory();
        StringBuilder sb = new StringBuilder();
        long a = va;
        for (int i = 0; i < 120; i++) {
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
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\strdump_report.txt"));
        str(0x00c221d8L);
        str(0x00c21bccL);
        str(0x00c21bdcL);
        str(0x00c21be4L);
        str(0x00c21becL);
        str(0x00c21bd4L);
        w.close();
        println("done: strdump_report.txt");
    }
}
