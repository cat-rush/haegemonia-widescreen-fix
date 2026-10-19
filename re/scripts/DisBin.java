import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;

public class DisBin extends GhidraScript {
    public void run() throws Exception {
        RandomAccessFile f = new RandomAccessFile("C:\\Users\\Nova\\haegemonia-mod\\rc_dump.bin", "r");
        byte[] data = new byte[(int)f.length()];
        f.readFully(data);
        f.close();
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rc_dis.txt"));
        long base = 0x7d3477L;
        for (int off = 0; off < data.length;) {
            try {
                var asm = disassemble(toAddr(base + off), data, off);
                if (asm == null || asm.isEmpty()) {
                    w.println(String.format("%08X: db %02X", base + off, data[off] & 0xff));
                    off++;
                    continue;
                }
                int len = 0;
                // GhidraScript disassemble(Address, byte[], int) returns list of strings
                // We use the flat API instead below; here just fall back.
                w.println(String.format("%08X: %s", base + off, asm));
                off++;
            } catch (Exception e) {
                w.println(String.format("%08X: db %02X", base + off, data[off] & 0xff));
                off++;
            }
        }
        w.close();
        println("done");
    }
}
