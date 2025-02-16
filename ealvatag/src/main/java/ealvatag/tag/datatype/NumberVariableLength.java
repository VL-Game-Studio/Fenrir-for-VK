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

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.tag.id3.ID3Tags;
import okio.Buffer;

/**
 * Represents a number which may span a number of bytes when written to file depending what size is to be represented.
 * <p>
 * The bitorder in ID3v2 is most significant bit first (MSB). The byteorder in multibyte numbers is most significant
 * byte first (e.g. $12345678 would be encoded $12 34 56 78), also known as big endian and network byte order.
 * <p>
 * In ID3Specification would be denoted as $xx xx xx xx (xx ...) , this denotes at least four bytes but may be more.
 * Sometimes may be completely optional (zero bytes)
 */
public class NumberVariableLength extends AbstractDataType {
    private static final int MINIMUM_NO_OF_DIGITS = 1;
    private static final int MAXIMUM_NO_OF_DIGITS = 8;

    int minLength = MINIMUM_NO_OF_DIGITS;


    /**
     * Creates a new ObjectNumberVariableLength datatype, set minimum length to zero
     * if this datatype is optional.
     *
     * @param identifier
     * @param frameBody
     * @param minimumSize
     */
    public NumberVariableLength(String identifier, AbstractTagFrameBody frameBody, int minimumSize) {
        super(identifier, frameBody);

        //Set minimum length, which can be zero if optional
        minLength = minimumSize;

    }

    public NumberVariableLength(NumberVariableLength copy) {
        super(copy);
        minLength = copy.minLength;
    }

    /**
     * Return the maximum number of digits that can be used to express the number
     *
     * @return the maximum number of digits that can be used to express the number
     */
    public int getMaximumLenth() {
        return MAXIMUM_NO_OF_DIGITS;
    }

    /**
     * Return the  minimum  number of digits that can be used to express the number
     *
     * @return the minimum number of digits that can be used to express the number
     */
    public int getMinimumLength() {
        return minLength;
    }

    /**
     * @param minimumSize
     */
    public void setMinimumSize(int minimumSize) {
        if (minimumSize > 0) {
            minLength = minimumSize;
        }
    }

    /**
     * @return the number of bytes required to write this to a file
     */
    public int getSize() {
        if (value == null) {
            return 0;
        } else {
            int current;
            long temp = ID3Tags.getWholeNumber(value);
            int size = 0;

            for (int i = MINIMUM_NO_OF_DIGITS; i <= MAXIMUM_NO_OF_DIGITS; i++) {
                current = (byte) temp & 0xFF;

                if (current != 0) {
                    size = i;
                }

                temp >>= MAXIMUM_NO_OF_DIGITS;
            }

            return (minLength > size) ? minLength : size;
        }
    }

    /**
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof NumberVariableLength)) {
            return false;
        }

        NumberVariableLength object = (NumberVariableLength) obj;

        return minLength == object.minLength && super.equals(obj);

    }

    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        //Coding error, should never happen
        if (arr == null) {
            throw new NullPointerException("Byte array is null");
        }

        //Coding error, should never happen as far as I can see
        if (offset < 0) {
            throw new IllegalArgumentException("negativer offset into an array offset:" + offset);
        }

        //If optional then set value to zero, this will mean that if this frame is written back to file it will be created
        //with this additional datatype wheras it didnt exist but I think this is probably an advantage the frame is
        //more likely to be parsed by other applications if it contains optional fields.
        //if not optional problem with this frame
        if (offset >= arr.length) {
            if (minLength == 0) {
                value = (long) 0;
                return;
            } else {
                throw new InvalidDataTypeException("Offset to byte array is out of bounds: offset = " +
                        offset +
                        ", array.length = " +
                        arr.length);
            }
        }

        long lvalue = 0;

        //Read the bytes (starting from offset), the most significant byte of the number being constructed is read first,
        //we then shift the resulting long one byte over to make room for the next byte
        for (int i = offset; i < arr.length; i++) {
            lvalue <<= 8;
            lvalue += (arr[i] & 0xff);
        }

        value = lvalue;
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        if (buffer.size() == 0) {
            if (minLength == 0) {
                value = (long) 0;
                return;
            } else {
                throw new InvalidDataTypeException("No data for value");
            }
        }

        //Read the bytes (starting from offset), the most significant byte of the number being constructed is read first,
        //we then shift the resulting long one byte over to make room for the next byte
        long lvalue = 0;
        for (int i = 0, readSize = (int) Math.min(size, buffer.size()); i < readSize; i++) {
            lvalue <<= 8;
            lvalue += (buffer.readByte() & 0xff);
        }

        value = lvalue;
    }

    /**
     * @return String representation of the number
     */
    @NonNull
    public String toString() {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * Write to Byte Array
     *
     * @return the datatype converted to a byte array
     */
    public byte[] writeByteArray() {
        int size = getSize();
        byte[] arr;

        if (size == 0) {
            arr = new byte[0];
        } else {
            long temp = ID3Tags.getWholeNumber(value);
            arr = new byte[size];

            //keeps shifting the number downwards and masking the last 8 bist to get the value for the next byte
            //to be written
            for (int i = size - 1; i >= 0; i--) {
                arr[i] = (byte) (temp & 0xFF);
                temp >>= 8;
            }
        }
        return arr;
    }
}
