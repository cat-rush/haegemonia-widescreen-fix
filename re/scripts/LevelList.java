import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LevelList extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\levellist_report.txt"));
        Memory mem = currentProgram.getMemory();
        String[] needles = {"game_inf/levels", "level.inf", "system.grd", ".grd", "multi_levels", "skirmish", "Level Multiplayer", "Levels/"};
        for (String needle : needles) {
            List<String> found = new ArrayList<String>();
            Address a = mem.getMinAddress();
            byte[] pat = needle.toLowerCase().getBytes("ASCII");
            long n = 0;
            while (a != null) {
                a = mem.findBytes(a, pat, null, true, monitor);
                if (a == null) break;
                if (!found.contains(a.toString())) found.add(a.toString());
                a = a.add(1);
                n++;
                if (n > 10000) break;
            }
            w.println("=== PATTERN \"" + needle + "\" (" + found.size() + " hits)");
            for (String s : found) {
                Address addr = toAddr(s);
                w.println("  str " + s + ": " + dumpString(addr));
                ReferenceManager rm = currentProgram.getReferenceManager();
                java.util.Iterator<Reference> it = rm.getReferencesTo(addr);
                int c = 0;
                while (it.hasNext() && c < 12) {
                    Reference r = it.next();
                    Address from = r.getFromAddress();
                    w.println("     xref from " + from + " " + (getFunctionContaining(from) != null ? getFunctionContaining(from).getName() : "?"));
                    c++;
                }
            }
        }
        w.close();
        println("done");
    }
    String dumpString(Address a) {
        StringBuilder sb = new StringBuilder();
        try {
            int i = 0;
            while (i < 256) {
                byte b = getByte(a.add(i));
                if (b == 0) break;
                sb.append((char)b);
                i++;
            }
        } catch (Exception e) { }
        return sb.toString();
    }
}
