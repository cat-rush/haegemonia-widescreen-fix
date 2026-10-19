import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ImportList extends GhidraScript {
    public void run() throws Exception {
        PrintWriter w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\importlist_report.txt"));
        Memory mem = currentProgram.getMemory();
        int dos = 0;
        try { dos = getShort(toAddr(0x400000L)) & 0xffff; } catch (Exception e) {}
        long ntOff = 0x400000L + getInt(toAddr(0x400000L + 0x3c)) & 0xFFFFFFFFL;
        int impRva = getInt(toAddr(ntOff + 0x80));
        long impOff = 0x400000L + impRva;
        long d = impOff;
        while (true) {
            int nameRva = getInt(toAddr(d + 12));
            if (nameRva == 0) break;
            long nameOff = 0x400000L + nameRva;
            byte[] nb = new byte[32];
            mem.getBytes(toAddr(nameOff), nb);
            StringBuilder sb = new StringBuilder();
            for (byte b : nb) { if (b == 0) break; sb.append((char)b); }
            String dll = sb.toString();
            int oftRva = getInt(toAddr(d + 16));
            int ftRva = getInt(toAddr(d + 20));
            long ftOff = 0x400000L + ftRva;
            for (int i = 0; ; i++) {
                int f = getInt(toAddr(ftOff + i * 4));
                if (f == 0) break;
                if ((f & 0x80000000L) != 0) continue;
                long ho = 0x400000L + f + 2;
                byte[] hb = new byte[64];
                try { mem.getBytes(toAddr(ho), hb); } catch (Exception e) { continue; }
                StringBuilder hs = new StringBuilder();
                for (byte b : hb) { if (b == 0) break; hs.append((char)b); }
                String fn = hs.toString();
                if (dll.toLowerCase().contains("kernel32") || dll.toLowerCase().contains("msvcr")) {
                    if (fn.toLowerCase().contains("read") || fn.toLowerCase().contains("file") ||
                        fn.toLowerCase().contains("open") || fn.toLowerCase().contains("map") ||
                        fn.toLowerCase().contains("fread") || fn.toLowerCase().contains("fopen")) {
                        w.println(dll + " : " + fn);
                    }
                }
            }
            d += 20;
        }
        w.close();
        println("done");
    }
}
