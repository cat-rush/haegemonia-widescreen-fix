import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SetRectSites extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\setrectsites_report.txt"));
        long[] sites = {0x43cff9L, 0x4474a2L, 0x517996L, 0x5208eeL, 0x7ddf79L};
        for (long s : sites) {
            Function f = getFunctionContaining(toAddr(s));
            w.println("==== site 0x" + Long.toHexString(s) + " in " +
                      (f != null ? f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() : "?"));
            if (f == null) continue;
            AddressSet set = new AddressSet(toAddr(Math.max(s - 0x60, f.getEntryPoint().getOffset())), toAddr(s));
            InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
            while (it.hasNext() && !monitor.isCancelled()) {
                Instruction ins = it.next();
                w.println(ins);
            }
        }
        w.close();
        println("done");
    }
}
