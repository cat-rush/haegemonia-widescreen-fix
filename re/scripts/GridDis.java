import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GridDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\griddis_report.txt"));
        long[][] ranges = {
            {0x783e30L, 0x783f80L},
            {0x782ee0L, 0x782f80L},
            {0x784300L, 0x784650L}
        };
        for (long[] r : ranges) {
            w.println("==== disasm " + Long.toHexString(r[0]) + ".." + Long.toHexString(r[1]));
            Address a = toAddr(r[0]);
            while (a.getOffset() <= r[1]) {
                var ins = currentProgram.getListing().getInstructionAt(a);
                if (ins == null) { a = a.add(1); continue; }
                w.println("  " + ins);
                a = ins.getDefaultFallThrough();
                if (a == null) break;
                if (monitor.isCancelled()) break;
            }
        }
        w.close();
        println("done");
    }
}
