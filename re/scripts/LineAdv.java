import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LineAdv extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\lineadv_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x004F0A57L, 0x004F014EL };
        for (long a : addrs) {
            Function f = getFunctionAt(toAddr(a));
            if (f != null) {
                w.println("---- " + f.getName() + " @ " + Long.toHexString(a) + " .. " + f.getBody().getMaxAddress() + " ----");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
