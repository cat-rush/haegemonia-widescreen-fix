// BoxArea3.java - disasm windows around the area-draw call sites to capture
// the authored design-space rect constants.
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea3 extends GhidraScript {
    void window(PrintWriter w, long site, long start, long end) throws Exception {
        w.println("== site " + Long.toHexString(site) + "  window " +
                  Long.toHexString(start) + ".." + Long.toHexString(end) + " ==");
        Address a = toAddr(start);
        while (a != null && a.getOffset() < end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("  " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }

    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea3_report.txt"));
        long[] sites = { 0x007819a9L, 0x00782ee4L, 0x00784a05L, 0x00784a57L, 0x00784aabL, 0x007cecdcL };
        for (long s : sites) window(w, s, s - 0x70, s + 0x10);
        w.close();
        println("done: boxarea3_report.txt");
    }
}
