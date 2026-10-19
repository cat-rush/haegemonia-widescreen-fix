import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import ghidra.program.model.scalar.Scalar;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AiOffScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\aioffscan_report.txt"));
        long[] offs = { 0x8cL, 0x90L, 0x94L, 0x98L, 0x1ccL, 0x1d4L, 0x1e0L, 0x1ecL };
        Listing listing = currentProgram.getListing();
        AddressSetView text;
        ghidra.program.model.mem.MemoryBlock blk = getMemoryBlock(".text");
        if (blk != null) {
            text = new AddressSet(blk.getStart(), blk.getEnd());
        } else {
            text = currentProgram.getMemory();
        }
        InstructionIterator it = listing.getInstructions(text, true);
        int count = 0;
        while (it.hasNext() && count < 60000) {
            Instruction ins = it.next();
            String s = ins.toString();
            for (long off : offs) {
                String hx = String.format("0x%x", off);
                if (s.contains(hx)) {
                    Function f = getFunctionContaining(ins.getAddress());
                    w.println(ins.getAddress().toString() + "  " + s + "   in " + (f != null ? f.getName() : "?"));
                    count++;
                    break;
                }
            }
        }
        w.println("total printed: " + count);
        w.close();
        println("done");
    }
}
