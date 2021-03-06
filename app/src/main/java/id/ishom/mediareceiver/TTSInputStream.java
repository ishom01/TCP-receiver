package id.ishom.mediareceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TTSInputStream extends DataInputStream {
    public TTSInputStream(InputStream in) {
        super(in);
    }

    public final int readFullyUntilEof(byte b[]) throws IOException {
        return readFullyUntilEof(b, 0, b.length);
    }

    public final int readFullyUntilEof(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                break;
            n += count;
        }
        return n;
    }
}