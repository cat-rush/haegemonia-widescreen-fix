import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgScreenCtx extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("msgscreenctx_report.txt"));
        ghidra.program.model.listing.Function f = getFunctionAt(toAddr(0x76bc4aL));
        if (f == null) { w.println("no fn"); w.close(); return; }
        ghidra.program.model.listing.Instruction inst = getInstructionAt(toAddr(0x76c5b0L));
        for (int i = 0; i < 60 && inst != null; i++) {
            w.println(inst.getAddress() + ": " + inst);
            inst = inst.getNext();
        }
        w.close();
    }
}
