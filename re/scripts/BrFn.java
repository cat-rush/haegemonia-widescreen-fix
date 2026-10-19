import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BrFn extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\brfn_report.txt"));
        ghidra.program.model.address.Address a = toAddr(0x0042c4a10L);
        ghidra.program.model.listing.Instruction inst = getInstructionAt(a);
        while (inst == null && a.getOffset() < 0x0042c600L) {
            a = a.add(1);
            inst = getInstructionAt(a);
        }
        FunctionManager fm = currentProgram.getFunctionManager();
        Function f = fm.getFunctionAt(a);
        if (f == null) {
            disassemble(a);
            f = createFunction(a, "BRPanelRenderer");
        }
        if (f != null) {
            w.println("function " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ")");
            DecompInterface d = new DecompInterface();
            d.openProgram(currentProgram);
            DecompileResults r = d.decompileFunction(f, 120, monitor);
            if (r != null && r.getDecompiledFunction() != null) {
                w.println(r.getDecompiledFunction().getC());
            } else {
                w.println("(decompile failed)");
            }
            d.dispose();
        }
        w.close();
        println("done");
    }
}
