import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.address.AddressIterator;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class PopupCallScan extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\popupcallscan_report.txt"));
        long[] targets = {0x0052388fL, 0x00525bf5L, 0x00525e9eL, 0x00525d66L,
                          0x0054e90fL, 0x00553274L, 0x0053bfdaL, 0x0052a0e0L,
                          0x0054f4eeL, 0x0054f5b6L, 0x0053db70L, 0x00538461L,
                          0x00529ca2L, 0x00529c29L};
        Map<Long,String> names = new HashMap<>();
        names.put(0x0052388fL, "popup widget ctor");
        names.put(0x00525bf5L, "FUN_00525bf5 (called at ctor end)");
        names.put(0x00525e9eL, "popup renderer");
        names.put(0x00525d66L, "popup clamp");
        names.put(0x0054e90fL, "popup update");
        names.put(0x00553274L, "tooltip opener");
        names.put(0x0053bfdaL, "mouse update (opens popups)");
        names.put(0x0052a0e0L, "popup row renderer");
        names.put(0x0054f4eeL, "FUN_0054f4ee");
        names.put(0x0054f5b6L, "FUN_0054f5b6");
        names.put(0x0053db70L, "FUN_0053db70");
        names.put(0x00538461L, "FUN_00538461");
        names.put(0x00529ca2L, "FUN_00529ca2");
        names.put(0x00529c29L, "FUN_00529c29");

        AddressSetView code = currentProgram.getMemory().getExecuteSet();
        TreeSet<String> out = new TreeSet<>();
        Instruction inst = getFirstInstruction();
        while (inst != null && !monitor.isCancelled()) {
            if (!code.contains(inst.getAddress())) {
                inst = getInstructionAfter(inst);
                continue;
            }
            String mn = inst.getMnemonicString();
            if (mn.equals("CALL") || mn.equals("JMP")) {
                for (Reference ref : inst.getReferencesFrom()) {
                    long tgt = ref.getToAddress().getOffset();
                    if (names.containsKey(tgt)) {
                        Function fn = getFunctionContaining(inst.getAddress());
                        out.add(String.format("0x%08x %s -> %s (0x%x)%s%n",
                            inst.getAddress().getOffset(), mn, names.get(tgt), tgt,
                            fn != null ? " in " + fn.getName() + " @" + fn.getEntryPoint() + " .. " + fn.getBody().getMaxAddress() : ""));
                    }
                }
            }
            inst = getInstructionAfter(inst);
        }
        w.println("==== direct CALL/JMP sites");
        for (String s : out) w.print(s);
        w.close();
        println("done");
    }
}
