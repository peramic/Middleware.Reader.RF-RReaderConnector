package havis.middleware.reader.rf_r;

public class RFCUtils {

	/**
	 * Converts a byte array to a hexadecimal string.
	 * 
	 * @param bytes
	 *            any array of bytes
	 * @return a hexadecimal string
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null)
			return null;

		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] resChars = new char[bytes.length * 2];

		for (int iByte = 0; iByte < bytes.length; iByte++) {
			byte b = bytes[iByte];
			int b0 = (b & 0xf0) >> 4;
			int b1 = b & 0x0f;

			resChars[2 * iByte] = hexChars[b0];
			resChars[2 * iByte + 1] = hexChars[b1];
		}
		return new String(resChars);
	}

	/**
	 * Writes the bits of the given byte array to std. out in 4-bit clusters.
	 * 
	 * @param bytes
	 *            an array of bytes
	 * @return a string showing the specified bytes as bits in well-readable
	 *         4-bit-clusters
	 */
	public static String bytesToBin(byte[] bytes) {
		if (bytes == null)
			return null;

		String[] binStrings = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010",
				"1011", "1100", "1101", "1110", "1111" };
		StringBuffer resultBuffer = new StringBuffer(bytes.length * 8 + 16);

		for (int iByte = 0; iByte < bytes.length; iByte++) {
			byte b = bytes[iByte];
			int b0 = (b & 0xf0) >> 4;
			int b1 = b & 0x0f;

			resultBuffer.append(binStrings[b0]);
			resultBuffer.append(" ");
			resultBuffer.append(binStrings[b1]);
			resultBuffer.append(" ");

		}
		return resultBuffer.toString().trim();

	}

	/**
	 * Converts the given hexadecimal string to a byte array.
	 * 
	 * @param hexStr
	 *            a hexadecimal string
	 * @return an array of bytes
	 * @throws IllegalArgumentException
	 *             if the hex string specified has an odd number of characters.
	 */
	public static byte[] hexToBytes(String hexStr) throws IllegalArgumentException {
		hexStr = hexStr.replaceAll("\\s|_", "");
		if (hexStr.length() % 2 != 0)
			throw new IllegalArgumentException("Hex string must have an even number of characters.");

		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length(); i += 2)
			result[i / 2] = Integer.decode("0x" + hexStr.charAt(i) + hexStr.charAt(i + 1)).byteValue();

		return result;
	}

	/**
	 * Converts an array of bytes to an integer. This array must not contain
	 * more that 4 bytes (32 bits), to make sure the result can be stored in an
	 * integer.
	 * 
	 * @param maxFourBytes
	 *            4 bytes of data
	 * @return the bytes of data as one integer
	 * @throws IllegalArgumentException
	 *             if the byte array contains more than 4 bytes.
	 */
	public static int bytesToInt(byte[] maxFourBytes) throws IllegalArgumentException {
		if (maxFourBytes.length > 4)
			throw new IllegalArgumentException("Byte array must not contain more than 4 bytes.");
		int ret = 0;
		for (int i = 0; i < maxFourBytes.length; i++) {
			ret |= maxFourBytes[i] & 0xff;
			if (i + 1 < maxFourBytes.length)
				ret <<= 8;
		}
		return ret;
	}

	/**
	 * Converts an array of bytes to a short. This array must not contain more
	 * that 2 bytes (16 bits), to make sure the result can be stored in a short.
	 * 
	 * @param maxTwoBytes
	 *            2 bytes of data
	 * @return the bytes of data as one short
	 * @throws IllegalArgumentException
	 *             if the byte array contains more than 2 bytes.
	 */
	public static short bytesToShort(byte[] maxTwoBytes) throws IllegalArgumentException {
		if (maxTwoBytes.length > 2)
			throw new IllegalArgumentException("Byte array must not contain more than 2 bytes.");
		short ret = 0;
		for (int i = 0; i < maxTwoBytes.length; i++) {
			ret |= maxTwoBytes[i] & 0xff;
			if (i + 1 < maxTwoBytes.length)
				ret <<= 8;
		}
		return ret;
	}

	/**
	 * Converts an integer to a byte array (of 4 bytes).
	 * 
	 * @param num
	 *            an integer
	 * @return 4 bytes of data representing the integer <code>num</code>
	 */
	public static byte[] intToBytes(int num) {
		byte[] arr = new byte[4];
		byte b = 0;
		for (int i = 4; i > 0; i--) {
			b = (byte) (num & 0xff);
			num >>= 8;
			arr[i - 1] = b;
		}
		return arr;
	}

	/**
	 * Converts a short to a byte array (of 2 bytes).
	 * 
	 * @param num
	 *            a short
	 * @return 2 bytes of data representing the short <code>num</code>
	 */
	public static byte[] shortToBytes(short num) {
		byte[] arr = new byte[2];
		byte b = 0;
		for (int i = 2; i > 0; i--) {
			b = (byte) (num & 0xff);
			num >>= 8;
			arr[i - 1] = b;
		}
		return arr;
	}

	/**
	 * Reverses the order of the elements in the given array.
	 * 
	 * @param array
	 *            an array of bytes
	 */
	public static void reverseByteArray(byte[] array) {
		reverseByteArray(array, 0, array.length);
	}

	/**
	 * Reverses the order of the elements in the given array starting at
	 * <code>startIndex</code> applying to <code>count</code> number of items.
	 * 
	 * @param array
	 *            an array of bytes
	 * @param startIndex
	 *            the index of the element to start to reverse the order
	 * @param count
	 *            the number of elements starting from <code>startIndex</code>
	 *            to apply this method to.
	 */
	public static void reverseByteArray(byte[] array, int startIndex, int count) {
		for (int l = startIndex, r = startIndex + count - 1; l < r; l++, r--) {
			byte item = array[l];
			array[l] = array[r];
			array[r] = item;
		}
	}
}
