package ealvatag.tag.id3;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ealvatag.tag.TagField;
import ealvatag.tag.TagTextField;
import ealvatag.utils.StandardCharsets;

/**
 * This class encapsulates the name and content of a tag entry in id3 fields
 * <br>
 *
 * @author @author Raphael Slinckx (KiKiDonK)
 * @author Christian Laireiter (liree)
 */
public class ID3v1TagField implements TagTextField {

    /**
     * Stores the id (name) of the tag field. <br>
     */
    private final String id;
    /**
     * If <code>true</code>, the id of the current encapsulated tag field is
     * specified as a common field. <br>
     * Example is "ARTIST" which should be interpreted by any application as the
     * artist of the media content. <br>
     * Will be set during construction with {@link #checkCommon()}.
     */
    private boolean common;
    /**
     * Stores the content of the tag field. <br>
     */
    private String content;

    /**
     * Creates an instance.
     *
     * @param raw Raw byte data of the tagfield.
     * @throws UnsupportedEncodingException If the data doesn't conform "UTF-8" specification.
     */
    public ID3v1TagField(byte[] raw) throws UnsupportedEncodingException {
        String field = new String(raw, StandardCharsets.ISO_8859_1);

        int i = field.indexOf('=');
        if (i == -1) {
            //Beware that ogg ID, must be capitalized and contain no space..
            id = "ERRONEOUS";
            content = field;
        } else {
            id = field.substring(0, i).toUpperCase();
            if (field.length() > i) {
                content = field.substring(i + 1);
            } else {
                //We have "XXXXXX=" with nothing after the "="
                content = "";
            }
        }
        checkCommon();
    }

    /**
     * Creates an instance.
     *
     * @param fieldId      ID (name) of the field.
     * @param fieldContent Content of the field.
     */
    public ID3v1TagField(String fieldId, String fieldContent) {
        id = fieldId.toUpperCase();
        content = fieldContent;
        checkCommon();
    }

    /**
     * This method examines the ID of the current field and modifies
     * {@link #common}in order to reflect if the tag id is a commonly used one.
     * <br>
     */
    private void checkCommon() {
        common = id.equals(ID3v1FieldKey.TITLE.name()) || id.equals(ID3v1FieldKey.ALBUM.name()) || id.equals(ID3v1FieldKey.ARTIST.name()) || id.equals(ID3v1FieldKey.GENRE.name()) || id.equals(ID3v1FieldKey.YEAR.name()) || id.equals(ID3v1FieldKey.COMMENT.name()) || id.equals(ID3v1FieldKey.TRACK.name());
    }

    /**
     * This method will copy all bytes of <code>src</code> to <code>dst</code>
     * at the specified location.
     *
     * @param src       bytes to copy.
     * @param dst       where to copy to.
     * @param dstOffset at which position of <code>dst</code> the data should be
     *                  copied.
     */
    protected void copy(byte[] src, byte[] dst, int dstOffset) {
        //        for (int i = 0; i < src.length; i++)
        //            dst[i + dstOffset] = src[i];
        /*
         * Heared that this method is optimized and does its job very near of
         * the system.
         */
        System.arraycopy(src, 0, dst, dstOffset, src.length);
    }

    @Override
    public void copyContent(TagField field) {
        if (field instanceof TagTextField) {
            content = ((TagTextField) field).getContent();
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String s) {
        content = s;
    }

    @Override
    public Charset getEncoding() {
        return StandardCharsets.ISO_8859_1;
    }

    @Override
    public void setEncoding(Charset s) {
        //Do nothing, encoding is always ISO-8859-1 for this tag
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public byte[] getRawContent() throws UnsupportedEncodingException {
        byte[] size = new byte[4];
        byte[] idBytes = id.getBytes(StandardCharsets.ISO_8859_1);
        byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
        byte[] b = new byte[4 + idBytes.length + 1 + contentBytes.length];

        int length = idBytes.length + 1 + contentBytes.length;
        size[3] = (byte) ((length & 0xFF000000) >> 24);
        size[2] = (byte) ((length & 0x00FF0000) >> 16);
        size[1] = (byte) ((length & 0x0000FF00) >> 8);
        size[0] = (byte) (length & 0x000000FF);

        int offset = 0;
        copy(size, b, offset);
        offset += 4;
        copy(idBytes, b, offset);
        offset += idBytes.length;
        b[offset] = (byte) 0x3D;
        offset++;// "="
        copy(contentBytes, b, offset);

        return b;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public void isBinary(boolean b) {
        //Do nothing, always false
    }

    @Override
    public boolean isCommon() {
        return common;
    }

    @Override
    public boolean isEmpty() {
        return "".equals(content);
    }

    @NonNull
    @Override
    public String toString() {
        return getContent();
    }
}
