package ealvatag.audio.real;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.Utils;
import ealvatag.audio.exceptions.CannotReadException;

public class RealChunk {

    protected static final String RMF = ".RMF";
    protected static final String PROP = "PROP";
    protected static final String MDPR = "MDPR";
    protected static final String CONT = "CONT";
    protected static final String DATA = "DATA";
    protected static final String INDX = "INDX";

    private final String id;
    private final int size;
    private final byte[] bytes;

    public RealChunk(String id, int size, byte[] bytes) {
        this.id = id;
        this.size = size;
        this.bytes = bytes;
    }

    public static RealChunk readChunk(RandomAccessFile raf)
            throws CannotReadException, IOException {
        String id = Utils.readString(raf, 4);
        int size = (int) Utils.readUint32(raf);
        if (size < 8) {
            throw new CannotReadException(
                    "Corrupt file: RealAudio chunk length at position "
                            + (raf.getFilePointer() - 4)
                            + " cannot be less than 8");
        }
        if (size > (raf.length() - raf.getFilePointer() + 8)) {
            throw new CannotReadException(
                    "Corrupt file: RealAudio chunk length of " + size
                            + " at position " + (raf.getFilePointer() - 4)
                            + " extends beyond the end of the file");
        }
        byte[] bytes = new byte[size - 8];
        raf.readFully(bytes);
        return new RealChunk(id, size, bytes);
    }

    public DataInputStream getDataInputStream() {
        return new DataInputStream(new ByteArrayInputStream(getBytes()));
    }

    public boolean isCONT() {
        return CONT.equals(id);
    }

    public boolean isPROP() {
        return PROP.equals(id);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @NonNull
    @Override
    public String toString() {
        return id + "\t" + size;
    }
}
