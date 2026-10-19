import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.scalar.Scalar;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BlHit4 extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\blhit4_report.txt"));
        for (Instruction ins : currentProgram.getListing().getInstructions(true)) {
            long va = ins.getAddress().getOffset();
            if (va < 0x00400000L || va > 0x00C00000L) continue;
            int n = ins.getNumOperands();
            boolean found = false;
            for (int i = 0; i < n; i++) {
                Object[] objs = ins.getOpObjects(i);
                for (Object o : objs) {
                    if (o instanceof Scalar) {
                        long v = ((Scalar) o).getUnsignedValue();
                        if (v >= 0x76440L && v <= 0x7646fL) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found) break;
            }
            if (!found) continue;
            Function f = getFunctionContaining(ins.getAddress());
            String fname = f == null ? "?" : f.getName() + " [" + f.getEntryPoint() + ".." + f.getBody().getMaxAddress() + "]";
            w.println(ins.getAddress() + " " + ins.toString() + "   in " + fname);
        }
        w.close();
        println("done");
    }
}
