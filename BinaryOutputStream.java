/**
 *  Author  :   Elbert Cheng
 *  Date    :   12/21/2019
 *  Version :   1.0.0
 *  Purpose :   To output n-bit numbers to a file. For example, this class can output a 9-bit number,
 *      or 11-bit, etc. To be used in conjunction with the BinaryInputStream class.
 */
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class BinaryOutputStream {
    // Byte value to denote the fixed size of a byte (8-bits).
    private static final int BYTE_SIZE = 8;

    // The file output stream to output the data through
    private BufferedOutputStream out;
    // Map which contains the value of different amount of 1's up to 8-bits
    private Map<Integer, Integer> onesMap;
    // The 8-bit integer buffer to be built up as the bits are read
    private int buffer;
    // An integer to keep track of the amount of bits built up in the buffer
    private byte status;

    /**
     * Initializes the BinaryOutputStream with a FileOutputStream.
     *
     * @param in    A FileOutputStream with the file to read to
     */
    public BinaryOutputStream(FileOutputStream in) {
        out = new BufferedOutputStream(in);
        onesMap = generateOnesMap(BYTE_SIZE);
        status = 0;
        buffer = 0;
    }

    /**
     * Initializes the BinaryOutputStream with a String, however the exception must be handled
     * outside the object initialization.
     *
     * @param file  String path of the output file
     * @throws FileNotFoundException The desired file is not found
     */
    public BinaryOutputStream(String file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    /******************
     * Public Methods *
     ******************/

    /**
     * Closes the underlying BufferedOutputStream.
     */
    public void close() {
        // Flush out the streams
        flush();
        // Close the streams
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pads the buffer with 0's then writes to the output file.
     */
    public void flush() {
        try {
            // Clean the buffer
            out.write(buffer);  // Buffer is full - WRITE TO OUTPUT
            // Flush the BufferedOutputStream
            out.flush();

            // Reset status and buffer
            status = 0;
            buffer = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes value "n" in the fashion of "l"-bits to the output file. If "l" is less
     * than the size of a byte then an IllegalArgumentException is thrown.
     *
     * @param n     The value of the number
     * @param l     The bit length of the number
     */
    public void writeBits(int n, int l) {
        if (l < BYTE_SIZE) throw new IllegalArgumentException("Length should not be less than the size of a byte.");

        // If the length is a byte and the buffer is empty
        if (status == 0 && l == BYTE_SIZE) {
            buffer = n;
            flush();
            return;
        }

        while (l > BYTE_SIZE) {
            // Calculate delta and add what's possible to the buffer
            int bDelta = BYTE_SIZE - status;
            buffer |= (n >> (l - bDelta));
            l -= bDelta;

            // Output it and clear buffer
            status += bDelta;
            // Buffer is full, flush it
            flush();
        }

        // If there's remaining, make it the buffer
        if (l > 0 && l <= BYTE_SIZE) {
            buffer = (n & onesMap.get(l));
            status = (byte) l;
            // Pad the buffer
            buffer <<= (BYTE_SIZE - status);
        }
    }

    /**************************
     * Private Method Helpers *
     **************************/

    /**
     * Generates a map with the number of bits as keys and its maximum value.
     * Example: [4 , "1111"] -> 15
     *
     * @param size  The maximum boundary of bits
     * @return      A Map<Integer, Integer>
     */
    private Map<Integer, Integer> generateOnesMap(int size) {
        StringBuffer str = new StringBuffer(size);
        Map<Integer, Integer> m = new HashMap<>(size);

        for (int i = 1; i <= size; i++) {
            str.append(1);
            m.put(i, Integer.parseInt(str.toString(), 2));
        }

        return m;
    }
}
