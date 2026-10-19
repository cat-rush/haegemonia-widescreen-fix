import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ListDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(f != null ? f.getEntryPoint().getOffset() : va) + " ----");
        if (f != null) {
            w.println("range: " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        }
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\listdump_report.txt"));
        dec(0x005205f5L);   /* settings/save list renderer */
        dec(0x005225d1L);   /* another list renderer (5225fc caller of 504948) */
        dec(0x00536a0fL);   /* planet/research screen */
        dec(0x00551ca5L);   /* picker (span table layout) */
        w.close();
        println("done");
    }
}
