import ghidra.app.script.GhidraScript;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgScreenDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("msgscreendis_report.txt"));
        ghidra.program.model.listing.Instruction inst = getInstructionAt(toAddr(0x76c5c0L));
        for (int i = 0; i < 70 && inst != null; i++) {
            w.println(inst.getAddress() + ": " + inst);
            inst = inst.getNext();
        }
        w.close();
    }
}
