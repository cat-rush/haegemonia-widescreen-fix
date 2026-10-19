import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Ai7c34bd extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\ai7c34bd_report.txt"));
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        Function f = getFunctionContaining(toAddr(0x7c34bdL));
        w.println("fn: " + (f != null ? f.getName() + " @ " + f.getEntryPoint() : "none"));
        if (f != null) {
            w.println("body: " + f.getBody().getMinAddress() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = di.decompileFunction(f, 180, monitor);
            if (res != null && res.decompileCompleted()) w.println(res.getDecompiledFunction().getC());
            else w.println("!!! decompile failed");
            w.println("");
            w.println("==== disasm ====");
            Instruction ins = getInstructionAt(f.getEntryPoint());
            while (ins != null && f.getBody().contains(ins.getAddress())) {
                w.println("  " + ins.getAddress().toString() + "  " + ins.toString());
                ins = ins.getNext();
            }
        }
        w.close();
        println("done");
    }
}
