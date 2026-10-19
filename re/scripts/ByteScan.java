import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ByteScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bytescan_report.txt"));
        byte[][] pats = {
            {(byte)0x40, (byte)0x11, 0, 0},        /* disp 0x1140 */
            {0, (byte)0x53, (byte)0x07, 0},        /* imm 0x75300 */
            {(byte)0x58, (byte)0x76, (byte)0x04, 0} /* 0x47658? */
        };
        MemoryBlock[] blocks = currentProgram.getMemory().getBlocks();
        for (MemoryBlock b : blocks) {
            if (!b.isExecute()) continue;
            byte[] buf = new byte[(int)b.getSize()];
            if (b.getBytes(b.getStart(), buf) != buf.length) continue;
            for (long off = 0; off + 4 <= buf.length; off++) {
                if (monitor.isCancelled()) break;
                for (byte[] p : pats) {
                    if (buf[(int)off] == p[0] && buf[(int)off+1] == p[1] &&
                        buf[(int)off+2] == p[2] && buf[(int)off+3] == p[3]) {
                        w.println("  hit " + b.getStart().add(off) + " pat " +
                                  String.format("%02x%02x%02x%02x", p[0], p[1], p[2], p[3]));
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
