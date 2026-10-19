// ContainDump.java - identify functions at frame RVA hits
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ContainDump extends GhidraScript {
    PrintWriter w;
    void container(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println("   0x" + Long.toHexString(va) + " inside " +
                  (f != null ? f.getName() + " @ " + f.getEntryPoint() : "?"));
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\contain_report.txt"));
        container(0x00691E55L);
        container(0x006C0976L);
        container(0x00521691L);
        container(0x0053A5C3L);
        container(0x006941a7L);
        container(0x005311b4L);
        container(0x00504948L);
        w.close();
        println("done: contain_report.txt");
    }
}
