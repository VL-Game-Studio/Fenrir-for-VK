/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.datatype;

import androidx.annotation.NonNull;

import java.io.EOFException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

public class Lyrics3Line extends AbstractDataType {
    /**
     *
     */
    private LinkedList<Lyrics3TimeStamp> timeStamp = new LinkedList<>();

    /**
     *
     */
    private String lyric = "";

    /**
     * Creates a new ObjectLyrics3Line datatype.
     *
     * @param identifier
     * @param frameBody
     */
    public Lyrics3Line(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    public Lyrics3Line(Lyrics3Line copy) {
        super(copy);
        lyric = copy.lyric;
        Lyrics3TimeStamp newTimeStamp;
        for (int i = 0; i < copy.timeStamp.size(); i++) {
            newTimeStamp = new Lyrics3TimeStamp(copy.timeStamp.get(i));
            timeStamp.add(newTimeStamp);
        }
    }

    /**
     * @return
     */
    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public void setLyric(ID3v2LyricLine line) {
        lyric = line.getText();
    }

    /**
     * @return
     */
    public int getSize() {
        int size = 0;
        for (Object aTimeStamp : timeStamp) {
            size += ((Lyrics3TimeStamp) aTimeStamp).getSize();
        }
        return size + lyric.length();
    }

    /**
     * @return
     */
    public Iterator<Lyrics3TimeStamp> getTimeStamp() {
        return timeStamp.iterator();
    }

    /**
     * @param time
     */
    public void setTimeStamp(Lyrics3TimeStamp time) {
        timeStamp.clear();
        timeStamp.add(time);
    }

    public void addLyric(String newLyric) {
        lyric += newLyric;
    }

    public void addLyric(ID3v2LyricLine line) {
        lyric += line.getText();
    }

    /**
     * @param time
     */
    public void addTimeStamp(Lyrics3TimeStamp time) {
        timeStamp.add(time);
    }

    /**
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Lyrics3Line)) {
            return false;
        }
        Lyrics3Line object = (Lyrics3Line) obj;
        if (!lyric.equals(object.lyric)) {
            return false;
        }
        return timeStamp.equals(object.timeStamp) && super.equals(obj);
    }

    /**
     * @return
     */
    public boolean hasTimeStamp() {
        return !timeStamp.isEmpty();
    }

    /**
     * @param lineString
     * @param offset
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void readString(String lineString, int offset) {
        if (lineString == null) {
            throw new NullPointerException("Image is null");
        }
        if ((offset < 0) || (offset >= lineString.length())) {
            throw new IndexOutOfBoundsException("Offset to line is out of bounds: offset = " + offset + ", line.length()" + lineString.length());
        }
        int delim;
        Lyrics3TimeStamp time;
        timeStamp = new LinkedList<>();
        delim = lineString.indexOf("[", offset);
        while (delim >= 0) {
            offset = lineString.indexOf("]", delim) + 1;
            time = new Lyrics3TimeStamp("Time Stamp");
            time.readString();
            timeStamp.add(time);
            delim = lineString.indexOf("[", offset);
        }
        lyric = lineString.substring(offset);
    }

    /**
     * @return
     */
    @NonNull
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Object aTimeStamp : timeStamp) {
            str.append(aTimeStamp.toString());
        }
        return "timeStamp = " + str + ", lyric = " + lyric + "\n";
    }

    /**
     * @return
     */
    public String writeString() {
        StringBuilder str = new StringBuilder();
        Lyrics3TimeStamp time;
        for (Lyrics3TimeStamp aTimeStamp : timeStamp) {
            time = aTimeStamp;
            str.append(time.writeString());
        }
        return str + lyric;
    }

    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        readString(Arrays.toString(arr), offset);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        readString(Arrays.toString(buffer.readByteArray()), 0);
    }

    public byte[] writeByteArray() {
        return writeString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
