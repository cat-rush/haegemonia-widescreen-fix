import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PbVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\pbvt_report.txt"));
        w.println("=== vtable 0xb31a5c (ProgressBar class)");
        for (int i = 0; i < 48; i++) {
            Address a = toAddr(0xb31a5cL + i * 4);
            int v = getInt(a);
            if (v >= 0x400000 && v < 0x400000 + 0xA00000) {
                w.println(String.format("   +%02X -> 0x%08X%s", i * 4, v,
                    getFunctionContaining(toAddr(v & 0xFFFFFFFFL)) != null
                    ? " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getName()
                      + " " + getFunctionContaining(toAddr(v & 0xFFFFFFFFL)).getBody() : ""));
            } else {
                w.println(String.format("   +%02X -> 0x%08X", i * 4, v));
            }
        }
        w.close();
        println("done");
    }
}
