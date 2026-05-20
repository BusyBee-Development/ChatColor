package net.busybee.chatcolor.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleFilterStream extends PrintStream {

    public ConsoleFilterStream(OutputStream out) {
        super(out, true, StandardCharsets.UTF_8);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        try {
            String str = new String(buf, off, len, StandardCharsets.UTF_8);
            String cleaned = ColorUtil.stripAll(str);
            byte[] cleanedBytes = cleaned.getBytes(StandardCharsets.UTF_8);
            super.write(cleanedBytes, 0, cleanedBytes.length);
        } catch (Exception e) {
            super.write(buf, off, len);
        }
    }
}
