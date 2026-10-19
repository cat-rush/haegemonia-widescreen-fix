import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Decoder for Haegemonia SCP files.
 * Format: uint32 LE uncompressed size, then bytes 0x78 0xDA, then a raw DEFLATE
 * stream (no zlib header, no Adler32 trailer).
 */
public class ScpDec {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("usage: ScpDec <file-or-dir> [outdir]");
            return;
        }
        Path in = Paths.get(args[0]);
        Path outDir = args.length > 1 ? Paths.get(args[1]) : null;
        if (Files.isDirectory(in)) {
            final Path inRoot = in;
            Files.walk(in).filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".scp"))
                 .forEach(p -> decodeOne(p, inRoot, outDir));
        } else {
            decodeOne(in, in.getParent(), outDir);
        }
    }

    static void decodeOne(Path p, Path inRoot, Path outDir) {
        try {
            byte[] raw = Files.readAllBytes(p);
            if (raw.length < 6) { System.out.println("SKIP (too small): " + p); return; }
            int declared = (raw[0] & 0xFF) | ((raw[1] & 0xFF) << 8) | ((raw[2] & 0xFF) << 16) | ((raw[3] & 0xFF) << 24);
            byte[] out;
            if ((raw[4] & 0xFF) == 0x78 && (raw[5] & 0xFF) == 0xDA) {
                Inflater inf = new Inflater(true);
                inf.setInput(raw, 6, raw.length - 6);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(declared > 0 ? declared : 1024);
                byte[] buf = new byte[65536];
                while (!inf.finished()) {
                    int n = inf.inflate(buf);
                    if (n == 0) throw new DataFormatException("truncated stream");
                    bos.write(buf, 0, n);
                }
                inf.end();
                out = bos.toByteArray();
            } else {
                // maybe not compressed
                out = Arrays.copyOfRange(raw, 4, raw.length);
            }
            if (declared > 0 && out.length != declared) {
                System.out.println("WARN size mismatch (" + declared + " vs " + out.length + "): " + p);
            }
            Path dest;
            if (outDir != null) {
                Path rel = inRoot.relativize(p);
                dest = outDir.resolve(rel.getParent() != null ? rel.getParent().resolve(p.getFileName().toString() + ".xml") : Paths.get(p.getFileName().toString() + ".xml"));
                if (dest.getParent() != null) Files.createDirectories(dest.getParent());
            } else {
                dest = p.resolveSibling(p.getFileName().toString() + ".xml");
            }
            Files.write(dest, out);
            System.out.println(String.format("%8d -> %s", out.length, dest));
        } catch (Exception e) {
            System.out.println("FAIL " + p + ": " + e);
        }
    }
}
