import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MsgVtDump extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\msgvt_report.txt"));
        Memory mem = currentProgram.getMemory();
        long vt = 0xb3ab9cL;
        for (int i = 0; i < 40; i++) {
            long a = vt + i * 4;
            int val = 0;
            try { val = mem.getInt(toAddr(a)); } catch (Exception e) { }
            Function fn = getFunctionContaining(toAddr((long)val & 0xFFFFFFFFL));
            w.println(String.format("  +0x%02X = %08X%s", i * 4, val,
                fn != null ? " -> " + fn.getName() + " " + fn.getBody() : ""));
        }
        w.close();
        println("done");
    }
}
