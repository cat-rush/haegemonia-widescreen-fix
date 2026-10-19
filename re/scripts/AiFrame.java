import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Variable;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Symbol;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiFrame extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aiframe_report.txt"));
        long[] fns = {0x007cb3f4L, 0x007c3968L, 0x007d1ef2L};
        for (long t : fns) {
            Function f = getFunctionContaining(toAddr(t));
            w.println("==== function " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : Long.toHexString(t)) + " ====");
            if (f == null) continue;
            w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress()
                + " stacksize=" + f.getStackFrame().getFrameSize());
            Variable[] vars = f.getAllVariables();
            for (Variable v : vars) {
                if (v.isStackVariable() && v.getStackOffset() < 0) {
                    w.println(String.format("  %-24s size=%d off=%d (0x%X)", v.getName(), v.getLength(), v.getStackOffset(), -v.getStackOffset()));
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
