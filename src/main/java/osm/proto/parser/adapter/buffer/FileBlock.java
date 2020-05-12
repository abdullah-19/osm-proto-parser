package osm.proto.parser.adapter.buffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Builder;
import lombok.Value;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Value
@Builder
public class FileBlock {
    DataInput dataInput;
    Long dataOffset;
    Integer dataSize;
    ByteString indexData;
    String type;

    public static FileBlock readFileBlock(FileInputStream fileInputStream) throws IOException {
        Long initialPosition = fileInputStream.getChannel().position();
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        int headerSize = dataInputStream.readInt();

        byte buffer[] = new byte[headerSize];
        dataInputStream.readFully(buffer);

        Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(buffer);

        return FileBlock.builder()
                .dataInput(dataInputStream)
                .dataOffset(initialPosition)
                .dataSize(blobHeader.getDatasize())
                .indexData(blobHeader.getIndexdata())
                .type(blobHeader.getType())
                .build();
    }

    public static FileBlock readFileBlock(File file, Long atPosition) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        randomAccessFile.seek(atPosition);
        int headerSize = randomAccessFile.readInt();

        byte buffer[] = new byte[headerSize];
        randomAccessFile.readFully(buffer);

        Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(buffer);

        return FileBlock.builder()
                .dataInput(randomAccessFile)
                .dataOffset(atPosition)
                .dataSize(blobHeader.getDatasize())
                .indexData(blobHeader.getIndexdata())
                .type(blobHeader.getType())
                .build();
    }

    public ByteString readBlockData() throws IOException {
        byte buffer[] = new byte[getDataSize()];
        dataInput.readFully(buffer);
        return parseBlockData(buffer);
    }

    private ByteString parseBlockData(byte buffer[]) throws InvalidProtocolBufferException {
        Fileformat.Blob blob = Fileformat.Blob.parseFrom(buffer);
        if (blob.hasRaw()) {
            return blob.getRaw();
        } else if (blob.hasZlibData()) {
            byte inflaterBuffer[] = new byte[blob.getRawSize()];
            Inflater inflater = new Inflater();
            inflater.setInput(blob.getZlibData().toByteArray());

            try {
                inflater.inflate(inflaterBuffer);
            } catch (DataFormatException e) {
                e.printStackTrace();
                throw new Error(e);
            }
            assert (inflater.finished());

            inflater.end();

            return ByteString.copyFrom(inflaterBuffer);
        }

        return null;
    }
}
