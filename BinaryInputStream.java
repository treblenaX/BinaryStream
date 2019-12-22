/**
 *  Author  :   Elbert Cheng
 *  Date    :   12/21/2019
 *  Version :   1.0.0
 *  Purpose :   To read in n-bit numbers from a file stream. For example, reading in 9 bits to create a number,
 *      then 11 bits, etc. To be used in conjunction with files generated with the BinaryOutputStream class.
 */
package BitStream;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.LinkedList;
import java.util.Queue;

public class BinaryInputStream {
    // Byte value to denote the end of the file.
    private static final byte EOF = -1;
    // Byte value to denote the fixed size of a byte (8-bits).
    private static final byte BYTE_SIZE = 8;

    // The file input stream to be used to read in the file
    private BufferedInputStream in;
    // The 8-bit integer buffer to be built up as the bits are read
    private int buffer;
    // An integer to keep track of the amount of bits built up in the buffer
    private int status;
    // A queue to hold the 8-bit chars to build up the buffer
    private Queue<Integer> bQueue;
    // A boolean to denote when the entire file is read
    private boolean stop;

    /**
     * Initializes the BinaryInputStream with a FileInputStream.
     *
     * @param fis   A FileInputStream focused on the file to be read
     */
    public BinaryInputStream(FileInputStream fis) {
        in = new BufferedInputStream(fis);
        bQueue = new LinkedList<>();
        stop = false;
    }

    /**
     * Initializes the BinaryInputStream with a String, however the exception must be handled
     * outside the object initialization.
     *
     * @param file  String path of the input file
     * @throws FileNotFoundException The desired file is not found
     */
    public BinaryInputStream(String file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    /******************
     * Public Methods *
     ******************/

    /**
     * Reads in "n" number of bits from "in" and returns the value calculated from those bits.
     *
     * @param n     The number of bits to be read from "in"
     * @return      The calculated integer value of the "n"-bits number
     */
    public int readBits(int n) {
        // If the buffer is empty, start it.
        if (status == 0) buffer = getNextBuffer(n);

        // If the end of the file is not reached, keep reading in 8-bit characters,
        // else return the EOF value.
        return (!stop) ? readBits(n, 0, n) : EOF;
    }

    /**
     * Closes the underlying BufferedInputStream.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**************************
     * Private Method Helpers *
     **************************/

    /**
     * Recursive private helper for "readBits".
     *
     * @param n     The number of bits to be read from "in"
     * @param t     The total value of the "n"-bits number
     * @param fL    A fixed value of "n"
     * @return      The calculated integer value of the "n"-bits number
     */
    private int readBits(int n, int t, int fL) {
        if (n == 0) return t;
        if (n >= BYTE_SIZE) {   // n is bigger than a byte - need get next 8-bit value
            // Push the current digits to the left to make room for the 8-bit combination
            // in the next step
            t |= (buffer << (n - BYTE_SIZE));

            // Subtract the status value from the length
            n -= status;

            // Remove the next 8-bit to replace the buffer
            int b = removeAndUpdate(fL);

            // If the stream hits an EOF, return the buffer if full then the EOF
            if (b == EOF) return (status == BYTE_SIZE) ? buffer : b;
            buffer = b;

            // Manipulate the byte status for the next step
            status = (n / BYTE_SIZE > 0) ? BYTE_SIZE : BYTE_SIZE - n;
        } else {    // n is less than a byte - combine into buffer
            int delta = BYTE_SIZE - status;

            // Combine the needed digits from the current buffer to t
            t |= (buffer >> (BYTE_SIZE - n));

            // Pad the buffer
            buffer <<= delta;

            // Make sure the buffer is only 8-bits
            buffer &= 255;

            // Subtract the number of digits used to the length
            n -= delta;
        }

        return readBits(n, t, fL);
    }

    /**
     * Removes the 8-bit integer in the queue and returns it.
     *
     * @param n     The number of bits to be read from "in"
     * @return      An 8-bit integer value
     */
    private int removeAndUpdate(int n) {
        return (bQueue.size() == 0) ? getNextBuffer(n) : bQueue.remove();
    }

    /**
     * Gets the next set of 8-bits to make the desired "n"-bit number.
     *
     * @param n     The number of bits to be read from "in"
     * @return      An 8-bit integer value
     */
    private int getNextBuffer(int n) {
        int i = status;
        while (i / n == 0) {    // While the number of 8-bits currently in the queue is not enough to make the "n"-bit value
            try {
                int b = in.read();

                if (b == EOF) { // At the end of the file - return the buffer if it's full
                    stop = true;
                    return b;
                }

                bQueue.add(b);
            } catch (IOException e) {
                e.printStackTrace();
            }

            i += BYTE_SIZE;
        }

        // Set the status to full
        status = BYTE_SIZE;

        // Return the next buffer to be in focus
        return bQueue.remove();
    }
}
