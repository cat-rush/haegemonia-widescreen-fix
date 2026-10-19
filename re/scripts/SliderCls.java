import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SliderCls extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\slidercls_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x0050D002L, 0x0050D3C9L, 0x0050D5C0L, 0x0050E183L };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f != null) {
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                w.println("---- " + f.getName() + " @ " + Long.toHexString(a) + " ----");
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                } else {
                    w.println("decompile failed");
                }
            } else {
                w.println("---- no function at " + Long.toHexString(a));
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
