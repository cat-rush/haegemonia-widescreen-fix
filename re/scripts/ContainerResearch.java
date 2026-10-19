// ContainerResearch.java - decompile the widgets with the reported
// container-gap problem (map labels, planet stats, production queue,
// empire top bar, tooltip) and dump their sources for metric analysis.
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ContainerResearch extends GhidraScript {

    PrintWriter w;

    void dec(long va) throws Exception {
        Function f = getFunctionAt(toAddr(va));
        w.println("---- " + (f != null ? f.getName() : "?") + " @ " + Long.toHexString(va) + " ----");
        if (f == null) { w.println("NO FUNCTION"); return; }
        DecompInterface di = new DecompInterface();
        di.openProgram(currentProgram);
        DecompileResults dr = di.decompileFunction(f, 300, monitor);
        if (dr == null || dr.getDecompiledFunction() == null) {
            w.println("DECOMPILE FAILED (null)");
        } else {
            w.println(dr.getDecompiledFunction().getC());
        }
        w.flush();
        di.dispose();
    }

    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\container_research_report.txt"));
        w.println("== MAP LABELS (world/map icons + label plates) ==");
        dec(0x00780fa1L);
        w.println("== PLANET WIDGET (planet screen stats) ==");
        dec(0x00536a0fL);
        w.println("== PLANET ICONS ==");
        dec(0x0074d20cL);
        w.println("== IN-GAME HUD / squad panel (production queue area) ==");
        dec(0x0077efc8L);
        w.println("== EMPIRE INFO TOP BAR ==");
        dec(0x0077ae14L);
        w.println("== TOOLTIP BUILDER ==");
        dec(0x00553274L);
        w.close();
        println("done: container_research_report.txt");
    }
}
