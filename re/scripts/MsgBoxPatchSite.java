import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgBoxPatchSite extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxpatch_report.txt"));
        Memory mem = currentProgram.getMemory();
        long start = 0x54a8d0L, end = 0x54dfdcL;
        for (long a = start; a < end - 12; a++) {
            byte[] b = new byte[12];
            mem.getBytes(toAddr(a), b);
            /* mov dword ptr [reg+0x20C], 0xC3C80000  = C7 ?? 0C 02 00 00 00 00 C8 C3 */
            if (b[2] == (byte)0x0C && b[3] == 0x02 && b[4] == 0x00 && b[5] == 0x00 &&
                b[6] == 0x00 && b[7] == 0x00 && b[8] == (byte)0xC8 && b[9] == (byte)0xC3) {
                w.println("FOUND [reg+0x20C]=-400.0 at VA 0x" + Long.toHexString(a) + " imm@0x" + Long.toHexString(a + 6));
            }
            /* any 0xC3C80000 little-endian immediate */
            if (b[0] == (byte)0xC8 && b[1] == (byte)0xC3) {
                w.println("imm -400.0 at VA 0x" + Long.toHexString(a) + " (imm starts)");
            }
        }
        w.close();
        println("done");
    }
}
