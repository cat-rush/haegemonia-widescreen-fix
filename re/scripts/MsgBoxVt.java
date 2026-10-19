import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgBoxVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgboxvt_report.txt"));
        for (long vt : new long[]{0xb3ab9cL, 0xb31d14L}) {
            w.println("==== vtable 0x" + Long.toHexString(vt));
            for (int i = 0; i < 24; i++) {
                long off = vt + i * 4;
                int v = 0;
                try { v = getInt(toAddr(off)); } catch (Exception e) { break; }
                Function f = getFunctionContaining(toAddr(v & 0xFFFFFFFFL));
                w.println("  [" + i + "] 0x" + Integer.toHexString(v) +
                          (f != null ? " = " + f.getName() + " (" + f.getEntryPoint() + ")" : ""));
            }
        }
        w.close();
        println("done");
    }
}
