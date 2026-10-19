import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RangeDis2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rangedis2_report.txt"));
        long[][] ranges = {
            {0x784300L, 0x7846a0L},
            {0x784f00L, 0x785460L}
        };
        for (long[] r : ranges) {
            w.println("==== disasm " + Long.toHexString(r[0]) + ".." + Long.toHexString(r[1]));
            AddressSet set = new AddressSet(toAddr(r[0]), toAddr(r[1]));
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
