import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RegDis extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\regdis_report.txt"));
        ghidra.program.model.address.Address start = toAddr(0x004815d0L);
        ghidra.program.model.address.Address end = toAddr(0x00481600L);
        ghidra.program.model.listing.Instruction inst = getInstructionContaining(start);
        while (inst != null && inst.getAddress().compareTo(end) < 0) {
            w.println(inst.getAddress() + ": " + inst.toString());
            inst = inst.getNext();
        }
        DecompInterface d = new DecompInterface();
        d.openProgram(currentProgram);
        Function f = getFunctionContaining(toAddr(0x004815d0L));
        if (f != null) {
            w.println("==== decompile ====");
            DecompileResults r = d.decompileFunction(f, 60, monitor);
            if (r != null && r.getDecompiledFunction() != null)
                w.println(r.getDecompiledFunction().getC());
        }
        d.dispose();
        w.close();
        println("done");
    }
}
