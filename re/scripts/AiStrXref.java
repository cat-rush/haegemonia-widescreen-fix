import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.address.Address;
import ghidra.program.model.mem.MemoryBlock;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiStrXref extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aistrxref_report.txt"));
        String[] names = { "AiPlayersPopup", "AiPlayersLabel", "AiLevelPopup" };
        MemoryBlock blk = getMemoryBlock(".data");
        if (blk == null) blk = getMemoryBlock(".rdata");
        if (blk == null) { w.println("no data block"); w.close(); return; }
        byte[] all = new byte[(int) blk.getSize()];
        blk.getBytes(blk.getStart(), all);
        for (String name : names) {
            byte[] nb = name.getBytes("ASCII");
            w.println("== " + name + " ==");
            outer:
            for (int i = 0; i <= all.length - nb.length; i++) {
                boolean ok = true;
                for (int j = 0; j < nb.length; j++) {
                    if (all[i + j] != nb[j]) { ok = false; break; }
                }
                if (ok) {
                    Address a = blk.getStart().add(i);
                    w.println("  string at " + a);
                    for (Reference r : getReferencesTo(a)) {
                        Function f = getFunctionContaining(r.getFromAddress());
                        w.println("    ref from " + r.getFromAddress() + " in " + (f != null ? f.getName() : "?"));
                    }
                    break outer;
                }
            }
            w.println("");
        }
        w.close();
        println("done");
    }
}
