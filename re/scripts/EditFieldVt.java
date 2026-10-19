import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class EditFieldVt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\editfieldvt_report.txt"));
        /* dump the vtable dwords at 0xb3179c */
        long vt = 0x00b3179cL;
        w.println("==== vtable PTR_FUN_00b3179c entries (EditField class)");
        for (int i = 0; i < 32; i++) {
            int d = getInt(toAddr(vt + i * 4));
            Function fn = getFunctionContaining(toAddr(d & 0xFFFFFFFFL));
            w.println(String.format("   slot %2d: 0x%08X%s", i, d,
                fn != null ? "  fn=" + fn.getName() + " @" + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
        }
        w.close();
        println("done");
    }
}
