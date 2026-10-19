import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MapCalls extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mapcalls_report.txt"));
        Function f = getFunctionAt(toAddr(0x00780fa1L));
        w.println("FUN_00780fa1: " + (f != null ? f.getEntryPoint() + " .. " + f.getBody().getMaxAddress() : "NO FN"));
        if (f != null) {
            Address a = f.getEntryPoint();
            long end = f.getBody().getMaxAddress().getOffset();
            while (a.getOffset() <= end) {
                Instruction ins = getInstructionAt(a);
                if (ins == null) break;
                String s = ins.toString();
                if (s.startsWith("CALL")) {
                    Object[] refs = ins.getReferencesFrom();
                    for (Object ro : refs) {
                        ghidra.program.model.symbol.Reference r = (ghidra.program.model.symbol.Reference)ro;
                        if (r.getReferenceType().isCall()) {
                            long t = r.getToAddress().getOffset();
                            if (t == 0x0044ecd6L || t == 0x0044efacL || t == 0x0044f648L ||
                                t == 0x0044728eL || t == 0x004471f6L || t == 0x00504948L) {
                                w.println("   " + a + "  " + ins + "  -> " + r.getToAddress());
                            }
                        }
                    }
                }
                a = ins.getAddress().add(ins.getLength());
            }
        }
        w.close();
        println("done");
    }
}
