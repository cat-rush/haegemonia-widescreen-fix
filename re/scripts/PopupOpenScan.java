import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PopupOpenScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popupopen_report.txt"));
        long[] targets = {0x00526ce8L, 0x00525d66L, 0x00529e3aL};
        String[] names = {"FUN_00526ce8 (popup open)", "FUN_00525d66 (clamp)", "FUN_00529e3a (item width)"};
        for (int t = 0; t < targets.length; t++) {
            w.println("==== direct CALL/JMP sites to " + names[t] + " (0x" + Long.toHexString(targets[t]) + ")");
            Instruction inst = getFirstInstruction();
            while (inst != null && !monitor.isCancelled()) {
                String mn = inst.getMnemonicString();
                if (mn.equals("CALL") || mn.equals("JMP")) {
                    for (Reference ref : inst.getReferencesFrom()) {
                        if (ref.getToAddress().getOffset() == targets[t]) {
                            Function fn = getFunctionContaining(inst.getAddress());
                            w.println("   0x" + Long.toHexString(inst.getAddress().getOffset())
                                + " " + mn
                                + (fn != null ? " in " + fn.getName() + " @" + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
                        }
                    }
                }
                inst = getInstructionAfter(inst);
            }
        }
        w.close();
        println("done");
    }
}
