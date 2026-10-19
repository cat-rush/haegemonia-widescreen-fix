import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class IconDis extends GhidraScript {
    PrintWriter w;
    void dis(long va, long end) throws Exception {
        Address a = toAddr(va);
        while (a.getOffset() < end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\icondis_report.txt"));
        w.println("== disasm 0x44ECD6..0x44F660 (find CALL 0x0044728e) ==");
        dis(0x0044ECD6L, 0x0044F660L);
        w.println("== function boundaries ==");
        long[] addrs = {0x0044ECD6L, 0x0044EFACL, 0x0044F648L, 0x0043518fL, 0x005311b4L};
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f != null) {
                w.println("   " + Long.toHexString(a) + " " + f.getName() + " .. " + f.getBody().getMaxAddress());
            }
        }
        w.close();
        println("done");
    }
}
