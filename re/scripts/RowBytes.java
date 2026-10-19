import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RowBytes extends GhidraScript {
    PrintWriter w;
    public void run() throws Exception {
        w = new PrintWriter(new FileWriter("C:\\Users\\Nova\\haegemonia-mod\\rowbytes_report.txt"));
        long a = 0x004f21c2L;
        Address ad = toAddr(a);
        byte[] b = new byte[32];
        currentProgram.getMemory().getBytes(ad, b);
        w.print(Long.toHexString(a) + ": ");
        for (int i = 0; i < 32; i++) w.print(String.format("%02X ", b[i] & 0xff));
        w.println();
        Function f = getFunctionAt(ad);
        if (f != null) w.println("fn " + f.getName() + " .. " + f.getBody().getMaxAddress());
        w.close();
        println("done");
    }
}
