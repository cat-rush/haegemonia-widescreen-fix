import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DpCallSite extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dpcallsite_report.txt"));
        long[][] ranges = {
            {0x73950L, 0x73980L},
            {0x4471f6L, 0x447240L},
            {0x44728eL, 0x4472e0L}
        };
        for (long[] rr : ranges) {
            w.println("==== 0x" + Long.toHexString(rr[0]) + " .. 0x" + Long.toHexString(rr[1]));
            Instruction ins = getInstructionAt(toAddr(rr[0]));
            while (ins != null && ins.getAddress().getOffset() <= rr[1]) {
                w.println(ins.getAddress() + "  " + ins);
                ins = getInstructionAfter(ins);
            }
        }
        w.close();
        println("done");
    }
}
