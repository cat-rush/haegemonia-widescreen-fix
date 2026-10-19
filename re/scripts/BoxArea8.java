// BoxArea8.java - decompile the designW-reading ship/camera helpers
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea8 extends GhidraScript {
    void window(PrintWriter w, long site, int before, int after) throws Exception {
        w.println("== window around " + Long.toHexString(site) + " ==");
        Address a = toAddr(site - before);
        Address end = toAddr(site + after);
        while (a != null && a.getOffset() < end.getOffset()) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            w.println("  " + ins);
            a = ins.getAddress().add(ins.getLength());
        }
        w.flush();
    }

    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea8_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] fns = { 0x004a0119L, 0x004a030dL, 0x004a04fbL, 0x004a89f3L, 0x004baa97L };
        for (long a : fns) {
            Function f = getFunctionContaining(toAddr(a));
            if (f == null) { w.println("==== " + Long.toHexString(a) + " : NO FUNCTION ===="); continue; }
            w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
            w.println("callers:");
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000L) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
            else w.println("(decompile failed)");
        }
        long[] sites = { 0x004a0164L, 0x004a0352L, 0x004a05f5L, 0x004a8aedL, 0x004bab32L };
        for (long s : sites) window(w, s, 48, 32);
        d.dispose();
        w.close();
        println("done: boxarea8_report.txt");
    }
}
