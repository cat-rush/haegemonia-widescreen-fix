import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TechFns extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\techfns_report.txt"));
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(
            toAddr(0x007d4000L), true);
        while (it.hasNext()) {
            Function f = it.next();
            if (f.getEntryPoint().getOffset() >= 0x007df000L) break;
            w.println(String.format("0x%08X..0x%08X %s", f.getEntryPoint().getOffset(),
                f.getBody().getMaxAddress().getOffset(), f.getName()));
        }
        w.close();
        println("done");
    }
}
