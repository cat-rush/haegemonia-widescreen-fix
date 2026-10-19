import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Vtable5168 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtable5168_report.txt"));
        long target = 0x00516877L;
        byte[] needle = new byte[4];
        needle[0] = (byte)(target & 0xff);
        needle[1] = (byte)((target >> 8) & 0xff);
        needle[2] = (byte)((target >> 16) & 0xff);
        needle[3] = (byte)((target >> 24) & 0xff);
        MemoryBlock[] blocks = currentProgram.getMemory().getBlocks();
        for (MemoryBlock b : blocks) {
            if (!b.isInitialized()) continue;
            long start = b.getStart().getOffset();
            long end = b.getEnd().getOffset();
            if (end - start > 0x800000) continue;
            byte[] data = new byte[(int)(end - start + 1)];
            b.getBytes(b.getStart(), data);
            for (int i = 0; i + 4 <= data.length; i += 4) {
                if (data[i] == needle[0] && data[i+1] == needle[1] && data[i+2] == needle[2] && data[i+3] == needle[3]) {
                    w.println("ptr to 0x516877 at VA " + Long.toHexString(start + i));
                }
            }
        }
        w.close();
        println("done");
    }
}
