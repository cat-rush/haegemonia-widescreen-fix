import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DisHudComposite extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\dishud_report.txt"));
        long[] starts = { 0x0071a1c4L, 0x0041b02dL, 0x00719f0aL };
        long[] ends   = { 0x0071a600L, 0x0041b300L, 0x0071a1c4L };
        for (int k = 0; k < 3; k++) {
            w.println("===== range " + Long.toHexString(starts[k]) + " .. " + Long.toHexString(ends[k]) + " =====");
            Address a = toAddr(starts[k]);
            Address end = toAddr(ends[k]);
            java.util.Iterator<Instruction> it = currentProgram.getListing().getInstructions(a, true);
            while (it.hasNext()) {
                Instruction ins = it.next();
                if (ins.getAddress().compareTo(end) > 0) break;
                w.println(ins.getAddress() + "  " + ins);
            }
        }
        w.println("== xrefs to 0x71a1c4 ==");
        ghidra.program.model.symbol.Reference[] r = getReferencesTo(toAddr(0x0071a1c4L));
        for (int i = 0; i < r.length; i++) w.println("  from " + r[i].getFromAddress());
        w.println("== xrefs to 0x41b02d ==");
        r = getReferencesTo(toAddr(0x0041b02dL));
        for (int i = 0; i < r.length; i++) w.println("  from " + r[i].getFromAddress());
        w.close();
        println("done");
    }
}
