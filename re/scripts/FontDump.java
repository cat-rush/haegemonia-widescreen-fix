import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FontDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    void dis(long va, long end, String title) throws Exception {
        w.println("== " + title + " ==");
        Address a = toAddr(va);
        while (a.getOffset() < end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("   " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\fontdump_report.txt"));
        dec(0x00435da3L);
        /* the loader's record area: from the 'INFO' magic check to the end */
        dis(0x00436A00L, 0x00436E80L, "loader disasm 0x436A00..0x436E80");
        /* scan loader for stores to [reg+0x18], [reg+0x1c], [reg+0x20], [reg+0x38], [reg+0x54], [reg+0x34], [reg+0x3c], [reg+0x4c] */
        Function f = getFunctionAt(toAddr(0x00435da3L));
        if (f != null) {
            w.println("== stores to font-field offsets inside loader ==");
            AddressSet body = new AddressSet(f.getBody());
            for (Address a : body.getAddresses(true)) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) continue;
                String s = ins.toString();
                if (s.contains("MOV") && s.contains("]") &&
                    (s.contains("+ 0x18]") || s.contains("+ 0x1c]") || s.contains("+ 0x20]") ||
                     s.contains("+ 0x38]") || s.contains("+ 0x34]") || s.contains("+ 0x3c]") ||
                     s.contains("+ 0x54]") || s.contains("+ 0x4c]") || s.contains("+ 0x18,") ||
                     s.contains("+ 0x1c,") || s.contains("+ 0x20,") || s.contains("+ 0x38,") ||
                     s.contains("+ 0x54,") || s.contains("+ 0x4c,"))) {
                    w.println("   " + a + "  " + ins);
                }
            }
            w.println("== FDIV/FILD sites in loader (u16 -> float division) ==");
            for (Address a : body.getAddresses(true)) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) continue;
                String s = ins.toString();
                if (s.startsWith("FDIV") || s.startsWith("FIDIV") || s.startsWith("FILD") ||
                    s.startsWith("FMUL") || s.startsWith("FSTP") || s.startsWith("FST")) {
                    w.println("   " + a + "  " + ins);
                }
            }
        }
        w.close();
        println("done");
    }
}
