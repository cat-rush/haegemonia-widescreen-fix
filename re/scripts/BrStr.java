import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.data.StringDataType;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrStr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brstr_report.txt"));
        long[] saddrs = { 0x00c06324L, 0x00c06330L, 0x00c06340L, 0x00c06348L };
        for (long a : saddrs) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 80; i++) {
                byte b = getByte(toAddr(a + i));
                if (b == 0) break;
                sb.append((char)(b & 0xff));
            }
            w.println(Long.toHexString(a) + " = '" + sb.toString() + "'");
        }
        ghidra.program.model.address.Address start = toAddr(0x0042c430L);
        ghidra.program.model.address.Address end = toAddr(0x0042c560L);
        ghidra.program.model.listing.Instruction inst = getInstructionContaining(start);
        while (inst != null && inst.getAddress().compareTo(end) < 0) {
            Function f = getFunctionContaining(inst.getAddress());
            String fn = f != null ? "  [fn " + f.getName() + "]" : "";
            w.println(inst.getAddress() + ": " + inst.toString() + fn);
            inst = inst.getNext();
        }
        w.close();
        println("done");
    }
}
