import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MesaVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("mesavt_report.txt"));
        long[] vts = { 0xb30924L, 0xb309b8L, 0xb30900L, 0xb308c4L, 0xb2f26cL, 0xb309f4L, 0xb30a14L };
        for (long v : vts) {
            w.println("==== vtable 0x" + Long.toHexString(v));
            for (int i = 0; i < 70; i++) {
                try {
                    int val = getInt(toAddr(v + i * 4));
                    if (val == 0) continue;
                    ghidra.program.model.listing.Function f = getFunctionAt(toAddr(val & 0xffffffffL));
                    w.println("  [" + i + "] 0x" + Integer.toHexString(val) +
                        (f != null ? " = " + f.getName() + " (0x" + Integer.toHexString((int)(f.getEntryPoint().getOffset())) + ")" : ""));
                } catch (Exception e) { w.println("  [" + i + "] ?"); }
            }
        }
        w.close();
    }
}
