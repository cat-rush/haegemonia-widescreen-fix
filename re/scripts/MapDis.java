import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MapDis extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mapdis_report.txt"));
        long[] ranges = {0x00780A80L, 0x00780B40L, 0x00781580L, 0x00781650L, 0x00782DC0L, 0x00782E10L, 0x00786050L, 0x00786150L};
        for (int r = 0; r < ranges.length; r += 2) {
            w.println("== disasm " + Long.toHexString(ranges[r]) + ".." + Long.toHexString(ranges[r+1]) + " ==");
            Address a = toAddr(ranges[r]);
            while (a.getOffset() < ranges[r+1]) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                w.println("   " + a + "  " + ins);
                a = ins.getAddress().add(ins.getLength());
            }
        }
        w.close();
        println("done");
    }
}
