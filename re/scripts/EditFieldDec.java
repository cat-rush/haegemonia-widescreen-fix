import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Instruction;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class EditFieldDec extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\editfield_report.txt"));
        DecompInterface ifc = new DecompInterface();
        ifc.openProgram(currentProgram);
        long[] starts = {0x005060ffL, 0x0050617cL, 0x00507a5dL};
        for (long s : starts) {
            Function f = getFunctionContaining(toAddr(s));
            if (f == null) { w.println("=== no fn at 0x" + Long.toHexString(s)); continue; }
            w.println("======== " + f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
            DecompileResults res = ifc.decompileFunction(f, 300, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
            } else {
                w.println("DECOMPILE FAILED");
            }
        }
        w.println("");
        w.println("==== direct CALL sites to FUN_005060ff (EditField ctor)");
        Instruction inst = getFirstInstruction();
        while (inst != null && !monitor.isCancelled()) {
            String mn = inst.getMnemonicString();
            if (mn.equals("CALL")) {
                for (Reference ref : inst.getReferencesFrom()) {
                    if (ref.getToAddress().getOffset() == 0x005060ffL) {
                        Function fn = getFunctionContaining(inst.getAddress());
                        w.println("   0x" + Long.toHexString(inst.getAddress().getOffset())
                            + (fn != null ? " in " + fn.getName() + " @" + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
                    }
                }
            }
            inst = getInstructionAfter(inst);
        }
        w.close();
        println("done");
    }
}
