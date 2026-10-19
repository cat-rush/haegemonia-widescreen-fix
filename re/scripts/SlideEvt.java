import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Instruction;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SlideEvt extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\slideevt_report.txt"));
        Function f = getFunctionContaining(toAddr(0x0052B36CL));
        w.println(f.getName() + " @ " + f.getEntryPoint() + " .. " + f.getBody().getMaxAddress());
        Address a = toAddr(0x0052B36CL);
        while (a.getOffset() < 0x0052B6E1L) {
            Instruction ins = getInstructionAt(a);
            if (ins == null) break;
            String s = ins.toString();
            if (s.contains("0xc68030") || s.contains("0xc68034") || s.contains("0xc0a790") || s.contains("0xc0a794") || s.contains("0xb3a848") || s.contains("0xb316")) {
                w.println("   " + ins.getAddress() + "  " + s);
            }
            a = ins.getAddress().add(ins.getLength());
        }
        w.close();
        println("done");
    }
}
