// VpCalls.java - find all call sites to the viewport setters 0x43FCAA /
// 0x43FBE0 + disasm FUN_0047352f around its viewport calls. Report -> vpcalls_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class VpCalls extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\vpcalls_report.txt"));
        long[] targets = { 0x43FCAA, 0x43FBE0, 0x4474A2, 0x48644E };
        for (long t : targets) {
            w.println("==== call/data refs to " + Long.toHexString(t) + " ====");
            try {
                Reference[] refs = getReferencesTo(toAddr(t));
                for (Reference ref : refs) {
                    long from = ref.getFromAddress().getOffset();
                    if (from < 0x400000L) continue;
                    Function cf = getFunctionContaining(ref.getFromAddress());
                    w.println("  " + ref.getFromAddress() + " type=" + ref.getReferenceType().getName()
                            + (cf != null ? " in " + cf.getName() : ""));
                }
            } catch (Exception e) { w.println("  EXC " + e.getMessage()); }
        }
        w.println("==== FUN_0047352f full disasm ====");
        Function f = getFunctionContaining(toAddr(0x47352fL));
        if (f != null) {
            Address a = f.getEntryPoint();
            Address end = f.getBody().getMaxAddress();
            while (a != null && a.getOffset() <= end.getOffset()) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                w.println("  " + ins);
                a = ins.getAddress().add(ins.getLength());
            }
        }
        w.close();
        println("done: vpcalls_report.txt");
    }
}
