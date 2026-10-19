import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Br6c4 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\br6c4_report.txt"));
        Function f = getFunctionContaining(toAddr(0x006c4b8fL));
        if (f != null) {
            w.println("fn " + f.getName() + " (" + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() + ")");
            w.println("callers:");
            ghidra.program.model.symbol.Reference[] refs = getReferencesTo(f.getEntryPoint());
            for (ghidra.program.model.symbol.Reference ref : refs) {
                if (ref.getFromAddress().getOffset() < 0x400000) continue;
                Function cf = getFunctionContaining(ref.getFromAddress());
                w.println("  " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() : ""));
            }
            w.println("callees:");
            ghidra.program.model.listing.Instruction inst = getInstructionAt(f.getEntryPoint());
            java.util.HashSet<Long> seen = new java.util.HashSet<Long>();
            while (inst != null && inst.getAddress().compareTo(f.getBody().getMaxAddress()) <= 0) {
                if (inst.getFlowType().isCall()) {
                    ghidra.program.model.address.Address[] flows = inst.getFlows();
                    for (ghidra.program.model.address.Address fl : flows) {
                        long t = fl.getOffset();
                        if (!seen.contains(t)) {
                            seen.add(t);
                            Function cal = getFunctionContaining(fl);
                            w.println("  call -> " + fl + (cal != null ? " (" + cal.getName() + ")" : ""));
                        }
                    }
                }
                inst = inst.getNext();
            }
        }
        w.close();
        println("done");
    }
}
