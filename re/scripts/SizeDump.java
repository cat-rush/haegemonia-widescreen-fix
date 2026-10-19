import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SizeDump extends GhidraScript {
    PrintWriter w;
    void fn(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println(Long.toHexString(va) + " in " + (f != null ? f.getName() + " @ " + f.getEntryPoint() + " size=" + f.getBody().getNumAddresses() : "?"));
        if (f != null) {
            Function n = getFunctionAfter(f.getEntryPoint());
            w.println("   next: " + (n != null ? n.getName() + " @ " + n.getEntryPoint() : "?"));
        }
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\size_report.txt"));
        fn(0x00780fa1L);
        fn(0x00536a0fL);
        w.close();
        println("done");
    }
}
