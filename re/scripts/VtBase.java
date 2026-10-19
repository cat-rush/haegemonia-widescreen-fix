import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VtBase extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vtbase_report.txt"));
        long vt = 0x00b2f664L;
        for (int i = 0; i < 45; i++) {
            long off = vt + i * 4L;
            int v = 0;
            try { v = getInt(toAddr(off)); } catch (Exception e) { break; }
            Function f = (v > 0x400000 && v < 0x4200000) ? getFunctionContaining(toAddr(v & 0xFFFFFFFFL)) : null;
            w.println("  [" + i + "] 0x" + Integer.toHexString(v) + (f != null ? " = " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ")" : ""));
        }
        /* the OBST handler = slot 28 */
        int obst = 0;
        try { obst = getInt(toAddr(vt + 28L * 4)); } catch (Exception e) {}
        w.println("OBST handler = 0x" + Integer.toHexString(obst));
        if (obst > 0x400000 && obst < 0x4200000) {
            Function f = getFunctionContaining(toAddr(obst & 0xFFFFFFFFL));
            if (f != null) {
                Instruction inst = getInstructionAt(f.getEntryPoint());
                int n = 0;
                while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0 && n < 120) {
                    w.println("  " + inst.getAddress() + ": " + inst.toString());
                    inst = inst.getNext();
                    n++;
                }
            }
        }
        w.close();
        println("done");
    }
}
