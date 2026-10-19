import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GridDis3 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\griddis3_report.txt"));
        long[][] ranges = {
            {0x783e60L, 0x7841d0L},
            {0x7843a0L, 0x784640L}
        };
        for (long[] r : ranges) {
            w.println("==== disasm " + Long.toHexString(r[0]) + ".." + Long.toHexString(r[1]));
            Address a = toAddr(r[0]);
            while (a.getOffset() <= r[1]) {
                var ins = currentProgram.getListing().getInstructionAt(a);
                if (ins == null) { a = a.add(1); continue; }
                w.println("  " + ins);
                Address next = ins.getDefaultFallThrough();
                if (next == null || next.getOffset() <= a.getOffset()) {
                    Address[] flows = ins.getFlows();
                    if (flows.length > 0) next = flows[0];
                }
                if (next == null || next.getOffset() <= a.getOffset() || next.getOffset() > r[1]) break;
                a = next;
                if (monitor.isCancelled()) break;
            }
        }
        w.close();
        println("done");
    }
}
