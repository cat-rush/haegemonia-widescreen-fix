import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;
import ghidra.program.model.mem.Memory;

public class FloatScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\floatscan_report.txt"));
        Memory mem = currentProgram.getMemory();
        long start = 0x00400000L;
        long end = 0x00C80000L;
        byte[] buf = new byte[(int)(end - start)];
        mem.getBytes(toAddr(start), buf);
        float[] pats = { 134.6f, 67.9f, 541.6f, 528.3f, 12.1f, 134.0f, 68.0f, 541.5f, 528.0f, 122.2f, 202.5f, 270.4f };
        for (float p : pats) {
            int bits = Float.floatToIntBits(p);
            byte[] pat = { (byte)(bits & 0xff), (byte)((bits >> 8) & 0xff),
                           (byte)((bits >> 16) & 0xff), (byte)((bits >> 24) & 0xff) };
            int count = 0;
            w.println("== float " + p + " (0x" + Integer.toHexString(bits) + ") ==");
            for (int i = 0; i + 4 <= buf.length; i++) {
                if (buf[i] == pat[0] && buf[i+1] == pat[1] && buf[i+2] == pat[2] && buf[i+3] == pat[3]) {
                    long off = start + i;
                    w.println("    0x" + Long.toHexString(off));
                    if (++count >= 40) { w.println("    ..."); break; }
                }
            }
        }
        w.close();
        println("done");
    }
}
