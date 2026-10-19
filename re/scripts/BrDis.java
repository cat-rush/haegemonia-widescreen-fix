import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brdis_report.txt"));
        ghidra.program.model.address.Address start = toAddr(0x0042c380L);
        ghidra.program.model.address.Address end = toAddr(0x0042c600L);
        ghidra.program.model.listing.Instruction inst = getInstructionContaining(start);
        while (inst != null && inst.getAddress().compareTo(end) < 0) {
            w.println(inst.toString());
            inst = inst.getNext();
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        long[] addrs = { 0x00711da4L, 0x00712c5eL };
        for (long a : addrs) {
            Function f = getFunctionContaining(toAddr(a));
            if (f != null) {
                w.println("==== " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ") ====");
                DecompileResults r = d.decompileFunction(f, 120, monitor);
                if (r != null && r.getDecompiledFunction() != null) {
                    w.println(r.getDecompiledFunction().getC());
                }
            }
        }
        d.dispose();
        w.close();
        println("done");
    }
}
