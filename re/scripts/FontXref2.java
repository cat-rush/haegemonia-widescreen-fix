import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FontXref2 extends GhidraScript {
    PrintWriter w;
    void decContaining(long va) throws Exception {
        Function f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(f != null ? f.getEntryPoint().getOffset() : va) + " (contains " + Long.toHexString(va) + ") ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
        w.println("== disasm " + f.getEntryPoint() + " .. +0x300 ==");
        Address a = f.getEntryPoint();
        long end = f.getEntryPoint().getOffset() + 0x300;
        while (a.getOffset() < end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fontxref2_report.txt"));
        decContaining(0x004390a5L);
        decContaining(0x004395bfL);
        w.close();
        println("done");
    }
}
