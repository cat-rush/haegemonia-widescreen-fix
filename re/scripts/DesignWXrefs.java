import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.AddressSet;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/* Find the corner-panel layout formula:
   x = (designW - 800) / 2 + authored, where designW = DAT_00c0a790.
   Look at every code reference to 0xc0a790 and dump the surrounding
   instructions + the containing function's address range, so the
   "(designW-800)/2" FPU sequence can be spotted. */
public class DesignWXrefs extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\designwxref_report.txt"));
        ReferenceManager rm = currentProgram.getReferenceManager();
        Iterator<Reference> it = rm.getReferencesTo(toAddr(0x00c0a790L));
        int c = 0;
        if (it == null) {
            w.println("no refs to 0xc0a790");
            w.close();
            return;
        }
        while (it.hasNext() && c < 60) {
            Reference r = it.next();
            Instruction ins = getInstructionAt(r.getFromAddress());
            Function fn = getFunctionContaining(r.getFromAddress());
            w.println("=== ref from " + r.getFromAddress() + " type=" + r.getReferenceType()
                      + (fn != null ? " fn=" + fn.getName() + " body=" + fn.getBody() : " ?"));
            if (ins != null) {
                AddressSet as = new AddressSet(ins.getAddress().subtract(48), ins.getAddress().add(64));
                Instruction di = getFirstInstruction();
                for (Instruction i = getInstructionAt(as.getMinAddress());
                     i != null && i.getAddress().compareTo(as.getMaxAddress()) <= 0;
                     i = i.getNext()) {
                    w.println("   " + i.getAddress() + "  " + i.toString());
                }
            }
            w.println();
            c++;
        }
        w.println("total shown: " + c);
        w.close();
        println("done");
    }
}
