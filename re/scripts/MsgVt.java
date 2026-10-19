import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgvt_report.txt"));
        long[] bases = {0xb30924L, 0xb308c4L, 0xb30900L, 0xb2f26cL, 0xb309b8L};
        for (long b : bases) {
            w.println("=== vtable 0x" + Long.toHexString(b));
            for (int i = 0; i < 40; i++) {
                Address a = toAddr(b + i * 4);
                int v = getInt(a);
                if (v >= 0x400000 && v < 0x400000 + 0xA00000) {
                    w.println(String.format("   +%02X -> 0x%08X%s", i * 4, v,
                        getFunctionContaining(toAddr(v & 0xFFFFFFFFL)) != null
                        ? " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getName()
                          + " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getBody() : ""));
                } else if (v != 0) {
                    w.println(String.format("   +%02X -> 0x%08X", i * 4, v));
                }
            }
        }
        w.close();
        println("done");
    }
}
