// FloatHunt.java - search .rdata/.data for the ship-container float constants
// (pos clusters from the [TF] diag: x ~ -660/-780/-221/-348/-503,
//  y ~ 1194.6/1192.7/1204.2, z ~ -375.4/-396/-318.7/536/622.8).
// Report matches + xrefs -> floathunt_report.txt
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.Reference;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FloatHunt extends GhidraScript {
    static int f2b(float f) {
        return Float.floatToRawIntBits(f);
    }
    void scan(PrintWriter w, String name, float f) throws Exception {
        int target = f2b(f);
        ghidra.program.model.mem.Memory mem = currentProgram.getMemory();
        ghidra.program.model.mem.MemoryBlock[] blocks = mem.getBlocks();
        for (ghidra.program.model.mem.MemoryBlock b : blocks) {
            if (!b.getName().contains("rdata") && !b.getName().contains("data")) continue;
            Address a = b.getStart();
            while (a != null && b.contains(a)) {
                Address faddr = mem.findBytes(a, b.getEnd(), bytesOf(target), null, true, monitor);
                if (faddr == null) break;
                w.println("  float " + name + " @ " + faddr);
                try {
                    Reference[] refs = getReferencesTo(faddr);
                    for (Reference ref : refs) {
                        w.println("      ref from " + ref.getFromAddress());
                    }
                } catch (Exception e) { w.println("      (xrefs n/a)"); }
                a = faddr.add(4);
            }
        }
    }
    byte[] bytesOf(int v) {
        return new byte[] { (byte)(v & 0xff), (byte)((v >> 8) & 0xff),
                            (byte)((v >> 16) & 0xff), (byte)((v >> 24) & 0xff) };
    }
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\floathunt_report.txt"));
        float[] xs = { -660.0f, -780.0f, -780.1f, -221.0f, -348.0f, -503.0f, -659.9f, -222.8f, -526.8f, -204.8f, -355.8f, -384.0f, -215.5f };
        float[] ys = { 1194.6f, 1192.7f, 1204.2f, 1205.1f, 1196.6f, 1210.5f, 1195.4f, 1170.7f, 1172.1f, 1212.3f, 1225.3f, 1175.1f };
        float[] zs = { -375.4f, -396.0f, -318.7f, 536.0f, 622.8f, -396.2f, -375.5f, -396.1f, -396.3f, 623.1f, 535.8f, -318.8f };
        w.println("==== x floats ====");
        for (float f : xs) scan(w, Float.toString(f), f);
        w.println("==== y floats ====");
        for (float f : ys) scan(w, Float.toString(f), f);
        w.println("==== z floats ====");
        for (float f : zs) scan(w, Float.toString(f), f);
        w.close();
        println("done: floathunt_report.txt");
    }
}

