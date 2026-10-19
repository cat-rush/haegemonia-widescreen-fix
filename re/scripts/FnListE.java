import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FnListE extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fnliste_report.txt"));
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
        int n = 0;
        while (it.hasNext()) {
            Function f = it.next();
            long e = f.getEntryPoint().getOffset();
            if (e >= 0x000d0000L && e <= 0x000f0000L) {
                w.println(f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
                n++;
            }
        }
        w.println("count=" + n);
        w.close();
        println("done");
    }
}
