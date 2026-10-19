// BoxArea15.java - disasm around the 400.0f constants
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BoxArea15 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\boxarea15_report.txt"));
        long[] sites = { 0x00418F3EL, 0x004E0B5BL, 0x0068F1ADL, 0x007EC694L, 0x00BA43DEL };
        for (long s : sites) {
            Function f = getFunctionContaining(toAddr(s));
            w.println("== " + Long.toHexString(s) + (f != null ? " in " + f.getName() + " @ " + f.getEntryPoint() : "") + " ==");
            Address a = toAddr(s - 0x30);
            Address end = toAddr(s + 0x20);
            while (a != null && a.getOffset() < end.getOffset()) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                w.println("  " + ins);
                a = ins.getAddress().add(ins.getLength());
            }
        }
        w.close();
        println("done: boxarea15_report.txt");
    }
}
