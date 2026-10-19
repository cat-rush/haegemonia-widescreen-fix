import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GridScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\gridscan_report.txt"));
        Function f = getFunctionContaining(toAddr(0x780fa1L));
        if (f == null) { w.println("fn not found"); w.close(); return; }
        Address a = f.getEntryPoint();
        Address end = f.getBody().getMaxAddress();
        while (a != null && a.getOffset() <= end.getOffset()) {
            var ins = currentProgram.getListing().getInstructionAt(a);
            if (ins == null) { a = a.add(1); continue; }
            String s = ins.toString();
            if (s.contains("0x1140") || s.contains("0x75300") || s.contains("0x12c00") ||
                s.contains("0x320") || s.contains("0x258") || s.contains("0x600")) {
                w.println("  " + ins);
            }
            a = ins.getDefaultFallThrough();
            if (monitor.isCancelled()) break;
        }
        w.close();
        println("done");
    }
}
