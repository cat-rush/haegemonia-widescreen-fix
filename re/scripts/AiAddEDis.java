import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiAddEDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fn828b49_dis.txt"));
        long[] addrs = { 0x828b49L, 0x828312L };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            w.println("========== DIS " + Long.toHexString(a) + " fn=" + (f != null ? f.getName() : "?") + " ==========");
            if (f != null) {
                w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
                Instruction ins = getInstructionAt(f.getEntryPoint());
                while (ins != null && f.getBody().contains(ins.getAddress())) {
                    w.println("  " + ins.getAddress().toString() + "  " + ins.toString());
                    ins = ins.getNext();
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
