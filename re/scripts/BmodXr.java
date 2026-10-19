import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class BmodXr extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\bmodxr_report.txt"));
        String[] tags = { "MESH", "VERT", "ISTR", "TRPS", "OBST", "MBOX" };
        for (String tag : tags) {
            w.println("==== tag '" + tag + "' ====");
            Address a = toAddr(0x400000L);
            while (a != null && a.getOffset() < 0x420000L + 0x980000L) {
                a = currentProgram.getListing().getDefinedDataAfter(a);
                if (a == null) break;
                if (a.getOffset() >= 0x420000L + 0x980000L) break;
                ghidra.program.model.listing.Data d = currentProgram.getListing().getDataAt(a);
                if (d != null && d.hasStringValue() && tag.equals(d.getDefaultValueRepresentation())) {
                    w.println("  data at " + a);
                    Reference[] refs = getReferencesTo(a);
                    for (Reference ref : refs) {
                        if (ref.getFromAddress().getOffset() < 0x400000) continue;
                        Function cf = getFunctionContaining(ref.getFromAddress());
                        w.println("    ref from " + ref.getFromAddress() + (cf != null ? " in " + cf.getName() + " (" + cf.getEntryPoint() + " .. " + cf.getBody().getMaxAddress() + ")" : ""));
                    }
                }
            }
        }
        w.close();
        println("done");
    }
}
