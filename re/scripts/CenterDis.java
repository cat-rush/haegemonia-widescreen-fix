import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

/* Disassemble FUN_004f3512 around the "center" attribute handling
   (the string refs at 0x4f3760 per the earlier scan) to find the
   layout computation for the centered attribute. */
public class CenterDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\centerdis_report.txt"));
        long[] starts = {0x004f3740L};
        for (long s : starts) {
            for (long a = s; a < s + 0x300; ) {
                Instruction i = getInstructionAt(toAddr(a));
                if (i == null) { a += 1; continue; }
                w.println(i.getAddress() + "  " + i.toString());
                a += i.getLength();
            }
        }
        w.close();
        println("done");
    }
}
