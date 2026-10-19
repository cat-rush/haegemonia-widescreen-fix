import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisRange2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\disrange2_report.txt"));
        long[] spans = { 0x0076d560L, 0x0076d5c0L, 0x0077f080L, 0x0077f170L };
        for (int i = 0; i < spans.length; i += 2) {
            Address a = toAddr(spans[i]);
            Address end = toAddr(spans[i + 1]);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
            while (it.hasNext()) {
                Instruction ins = it.next();
                if (ins.getAddress().compareTo(end) > 0) break;
                w.println(ins.getAddress() + "  " + ins);
            }
            w.println("----");
        }
        w.close();
        println("done");
    }
}
