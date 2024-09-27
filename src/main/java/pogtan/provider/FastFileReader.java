package pogtan.provider;

import pogtan.util.Tuple;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FastFileReader implements AutoCloseable {
    private final RandomAccessFile file;
    private final FileChannel channel;

    public FastFileReader(RandomAccessFile file, FileChannel channel) {
        this.file = file;
        this.channel = channel;
    }

    public ByteBuffer getBuffer(int offset) throws IOException {
        final ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, file.length());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    public Map<String, Tuple<Integer, Integer>> readIndex() throws IOException, ProviderError {
        // CFastFile::LoadIdx
        final Map<String, Tuple<Integer, Integer>> index = new HashMap<>();
        final ByteBuffer buffer = getBuffer(0);
        final int count = buffer.getInt();
        if (count <= 0) {
            throw new ProviderError("Invalid count %d", count);
        }
        for (int i = 0; i < count; i++) {
            final int nameLength = buffer.getInt();
            if (nameLength <= 0 || Integer.toUnsignedLong(nameLength) >= 0x80) {
                throw new ProviderError("Invalid name length %d", nameLength);
            }
            final byte[] nameArray = new byte[nameLength];
            buffer.get(nameArray);
            final String name = new String(nameArray, StandardCharsets.US_ASCII);
            final int offset = buffer.getInt();
            final int size = buffer.getInt();
            index.put(name, Tuple.of(offset, size));
        }
        return Collections.unmodifiableMap(index);
    }

    @Override
    public void close() throws Exception {
        file.close();
        channel.close();
    }

    public static FastFileReader build(String path) throws FileNotFoundException {
        return FastFileReader.build(new File(path));
    }

    public static FastFileReader build(Path path) throws FileNotFoundException {
        return FastFileReader.build(path.toFile());
    }

    public static FastFileReader build(File file) throws FileNotFoundException {
        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        final FileChannel channel = randomAccessFile.getChannel();
        return new FastFileReader(randomAccessFile, channel);
    }
}