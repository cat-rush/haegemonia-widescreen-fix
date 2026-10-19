import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TweakDump extends GhidraScript {
    PrintWriter w;
    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        if (f == null) f = getFunctionContaining(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(f != null ? f.getEntryPoint().getOffset() : va) + " ----");
        if (f != null) {
            w.println("range: " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + " size=" + f.getBody().getNumAddresses());
        }
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 120, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) { w.println("FAIL (null)"); di.dispose(); return; }
        w.println(dr.getDecompiledFunction().getC());
        w.flush(); di.dispose();
    }
    void callsIn(long fva, String title) throws Exception {
        Function f = getFunctionAt(toAddr(fva));
        if (f == null) { w.println("== " + title + ": NO FN =="); return; }
        w.println("== " + title + " (call targets in " + f.getName() + ") ==");
        Address a = f.getEntryPoint();
        long end = f.getBody().getMaxAddress().getOffset();
        while (a.getOffset() <= end) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            String s = ins.toString();
            if (s.startsWith("CALL")) {
                Object[] refs = ins.getReferencesFrom();
                for (Object ro : refs) {
                    ghidra.program.model.symbol.Reference r = (ghidra.program.model.symbol.Reference)ro;
                    if (r.getReferenceType().isCall()) {
                        w.println("   " + a + "  " + ins + "  -> " + r.getToAddress());
                    }
                }
            }
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\tweakdump_report.txt"));
        dec(0x005311b4L);                       /* map widget */
        callsIn(0x005311b4L, "calls inside FUN_005311b4");
        dec(0x0043518fL);                       /* cursor */
        callsIn(0x0043518fL, "calls inside cursor fn");
        dec(0x004f0a57L);                       /* text system */
        dec(0x004ef060L);                       /* text funnel */
        w.close();
        println("done");
    }
}
