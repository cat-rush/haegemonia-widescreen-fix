import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgAsm2 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgasm2_report.txt"));
        long[][] ranges = {
            {0x722540L, 0x7226A0L},
            {0x7228a0L, 0x722b00L},
            {0x722b00L, 0x722d1cL}
        };
        for (long[] r : ranges) {
            w.println("==== disasm " + Long.toHexString(r[0]) + ".." + Long.toHexString(r[1]));
            AddressSet set = new AddressSet(toAddr(r[0]), toAddr(r[1]));
            InstructionIterator it = currentProgram.getListing().getInstructions(set, true);
            int n = 0;
            while (it.hasNext() && !monitor.isCancelled()) {
                Instruction ins = it.next();
                w.println(ins);
                if (++n > 700) break;
            }
        }
        w.close();
        println("done");
    }
}
