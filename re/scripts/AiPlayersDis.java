import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSetView;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiPlayersDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aiplayersdis_report.txt"));
        Function f = getFunctionContaining(toAddr(0x007ace1bL));
        if (f == null) { w.println("no function"); w.close(); return; }
        AddressSetView body = f.getBody();
        w.println("FUN_007ace1b body: " + body.getMinAddress() + " .. " + body.getMaxAddress() + " size=" + body.getNumAddresses());
        long[] refs = {0x007b2877L, 0x007b2973L};
        for (long r : refs) {
            w.println("");
            w.println("==== disasm window around " + Long.toHexString(r) + " ====");
            Address a = toAddr(r);
            Address lo = a.subtract(320);
            Address hi = a.add(640);
            Instruction inst = currentProgram.getListing().getInstructionAt(lo);
            while (inst != null && inst.getAddress().compareTo(hi) <= 0) {
                w.println(inst.getAddress().toString() + "  " + inst.toString());
                inst = currentProgram.getListing().getInstructionAfter(inst.getAddress());
            }
        }
        w.close();
        println("done");
    }
}
