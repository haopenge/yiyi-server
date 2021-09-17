package com.peppa.common.security;


public class Hex {
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }


    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }


    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0xF & data[i]];
        }
        return out;
    }


    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }


    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }


    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }


    public static byte[] decodeHex(char[] data) {
        int len = data.length;
        if ((len & 0x1) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];


        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f |= toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }


    private static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }


    public static String bytes2Hex(byte[] inbuf) {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < inbuf.length; i++) {
            String byteStr = Integer.toHexString(inbuf[i] & 0xFF);
            if (byteStr.length() != 2) {
                strBuf.append('0').append(byteStr);
            } else {
                strBuf.append(byteStr);
            }
        }
        return new String(strBuf);
    }


    public static byte[] hexToBytes(String inbuf) {
        int len = inbuf.length() / 2;
        byte[] outbuf = new byte[len];
        for (int i = 0; i < len; i++) {
            String tmpbuf = inbuf.substring(i * 2, i * 2 + 2);
            outbuf[i] = (byte) Integer.parseInt(tmpbuf, 16);
        }
        return outbuf;
    }
    /*     */
}

