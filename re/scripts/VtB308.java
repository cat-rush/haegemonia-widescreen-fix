import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtB308 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtb308_report.txt"));
        long vt = 0x00b30824L;
        for (int i = 0; i < 60; i++) {
            long off = vt + i * 4L;
            int v = 0;
            try { v = getInt(toAddr(off)); } catch (Exception e) { break; }
            Function f = (v > 0x400000 && v < 0x4200000) ? getFunctionContaining(toAddr(v & 0xFFFFFFFFL)) : null;
            w.println("  [" + i + "] 0x" + Integer.toHexString(v) + (f != null ? " = " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ")" : ""));
        }
        w.close();
        println("done");
    }
}
