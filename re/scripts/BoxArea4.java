// BoxArea4.java - first bytes of the box-scene renderers (for entry hooks)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea4 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea4_report.txt"));
        long[] starts = { 0x0047352fL, 0x00473078L, 0x0047523aL, 0x00447337L, 0x004aaa32L };
        for (long s : starts) {
            w.println("== " + Long.toHexString(s) + " ==");
            Address a = toAddr(s);
            for (int i = 0; i < 12; i++) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                w.println("  " + ins);
                a = ins.getAddress().add(ins.getLength());
            }
        }
        w.close();
        println("done: boxarea4_report.txt");
    }
}
