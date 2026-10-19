// BoxArea9.java - identify the ship-list projector callers + prologues
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea9 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea9_report.txt"));
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);

        long[] heads = { 0x0079f3e8L, 0x007e176dL, 0x0079d304L, 0x0078ea61L,
                         0x0078f426L, 0x007a03b4L, 0x00713508L, 0x007133fcL,
                         0x0079d25bL, 0x007a34e0L, 0x007a37f6L };
        for (long a : heads) {
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
            DecompileResults r = d.decompileFunction(f, 60, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                String c = r.getDecompiledFunction().getC();
                String[] lines = c.split("\n");
                int n = Math.min(lines.length, 45);
                for (int i = 0; i < n; i++) w.println(lines[i]);
                if (lines.length > n) w.println("   ... (truncated)");
            }
        }

        w.println("==== prologues ====");
        long[] pro = { 0x004a0119L, 0x004a030dL };
        for (long s : pro) {
            w.println("-- " + Long.toHexString(s) + " --");
            Address a = toAddr(s);
            for (int i = 0; i < 8; i++) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                w.println("  " + ins);
                a = ins.getAddress().add(ins.getLength());
            }
        }
        d.dispose();
        w.close();
        println("done: boxarea9_report.txt");
    }
}
