import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.DataIterator;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MpScreenFind extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\mpscreen_report.txt"));
        DataIterator it = currentProgram.getListing().getDefinedData(true);
        int n = 0;
        while (it.hasNext() && n < 200) {
            Data d = it.next();
            Object v = d.getValue();
            if (!(v instanceof String)) continue;
            String s = (String)v;
            String ls = s.toLowerCase();
            if (!(ls.contains("ultiplayer") || ls.contains("kirmish") ||
                  ls.contains("mpmenu") || ls.contains("mpscreen") ||
                  ls.contains("mplevel") || ls.contains("game setup") ||
                  ls.contains("players"))) continue;
            w.println("STR @" + d.getAddress() + ": " + s);
            int r = 0;
            for (Reference ref : getReferencesTo(d.getAddress())) {
                Function f = getFunctionContaining(ref.getFromAddress());
                w.println("   xref " + ref.getFromAddress() + " in " +
                          (f != null ? f.getName() + " (" + f.getEntryPoint() + ")" : "?"));
                if (++r >= 8) { w.println("   ..."); break; }
            }
            n++;
        }
        w.close();
        println("done");
    }
}
