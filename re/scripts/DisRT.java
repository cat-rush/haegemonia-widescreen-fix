import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisRT extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\disrt_report.txt"));
        long[] starts = { 0x0071a940L, 0x004bffe0L, 0x007262a0L, 0x0071a950L, 0x005b9266L };
        long[] ends   = { 0x0071ac00L, 0x004c0200L, 0x00726380L, 0x0071a9c0L, 0x005b9400L };
        for (int k = 0; k < 5; k++) {
            w.println("===== FUN " + Long.toHexString(starts[k]) + " =====");
            Address a = toAddr(starts[k]);
            Address end = toAddr(ends[k]);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
            while (it.hasNext()) {
                Instruction ins = it.next();
                if (ins.getAddress().compareTo(end) > 0) break;
                w.println(ins.getAddress() + "  " + ins);
            }
        }
        w.println("== xrefs to 0x71a940 ==");
        ghidra.program.model.symbol.Reference[] r = getReferencesTo(toAddr(0x0071a940L));
        for (int i = 0; i < r.length; i++) w.println("  from " + r[i].getFromAddress());
        w.close();
        println("done");
    }
}
