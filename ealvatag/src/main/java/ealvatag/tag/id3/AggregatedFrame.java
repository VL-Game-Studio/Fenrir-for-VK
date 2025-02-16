package ealvatag.tag.id3;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

import ealvatag.tag.TagField;
import ealvatag.tag.TagTextField;
import ealvatag.tag.id3.valuepair.TextEncoding;

/**
 * Required when a single generic field maps to multiple ID3 Frames
 */
public class AggregatedFrame implements TagTextField {
    //TODO rather than just maintaining insertion order we want to define a preset order
    protected Set<AbstractID3v2Frame> frames = new LinkedHashSet<>();

    public void addFrame(AbstractID3v2Frame frame) {
        frames.add(frame);
    }

    public Set<AbstractID3v2Frame> getFrames() {
        return frames;
    }

    /**
     * Returns the content of the underlying frames in order.
     *
     * @return Content
     */
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (AbstractID3v2Frame next : frames) {
            sb.append(next.getContent());
        }
        return sb.toString();
    }

    /**
     * Sets the content of the field.
     *
     * @param content fields content.
     */
    public void setContent(String content) {

    }

    /**
     * Returns the current used charset encoding.
     *
     * @return Charset encoding.
     */
    public Charset getEncoding() {
        byte textEncoding = frames.iterator().next().getBody().getTextEncoding();
        return TextEncoding.getInstanceOf().getCharsetForId(textEncoding);
    }

    /**
     * Sets the charset encoding used by the field.
     *
     * @param encoding charset.
     */
    public void setEncoding(Charset encoding) {

    }

    //TODO:needs implementing but not sure if this method is required at all
    public void copyContent(TagField field) {

    }

    public String getId() {
        StringBuilder sb = new StringBuilder();
        for (AbstractID3v2Frame next : frames) {
            sb.append(next.getId());
        }
        return sb.toString();
    }


    public boolean isCommon() {
        return true;
    }

    public boolean isBinary() {
        return false;
    }

    public void isBinary(boolean b) {
    }

    public boolean isEmpty() {
        return false;
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        throw new UnsupportedEncodingException();
    }
}
