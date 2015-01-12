package tesler.will.wav;


public class ByteUtils {

	// these two routines convert a byte array to a unsigned short
	public static int byteArrayToInt(byte[] b) {
		int start = 0;
		int low = b[start] & 0xff;
		int high = b[start + 1] & 0xff;
		return (int) (high << 8 | low);
	}

	// these two routines convert a byte array to a unsigned short
	public static short byteArrayToShort(byte[] b) {
		int start = 0;
		short low = (short) (b[start] & 0xff);
		short high = (short) (b[start + 1] & 0xff);
		return (short) (high << 8 | low);
	}

	// these two routines convert a byte array to an unsigned integer
	public static long byteArrayToLong(byte[] b) {
		int start = 0;
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = b[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return accum;
	}

	public static String byteArrayToString(byte[] bytes) {
		StringBuilder sBuilder = new StringBuilder(bytes.length);
		for (byte b : bytes) {
			sBuilder.append((char) b);
		}
		return sBuilder.toString();
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short sample) {
		return new byte[] { (byte) (sample & 0xff), (byte) ((sample >>> 8) & 0xff) };
	}

	public static byte[] floatToByteArray(Float sample) {
		return intToByteArray(Float.floatToIntBits(sample));
	}

	public static byte[] intToByteArray(int bits) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (bits & 0x00FF);
		bytes[1] = (byte) ((bits >> 8) & 0x000000FF);
		bytes[2] = (byte) ((bits >> 16) & 0x000000FF);
		bytes[3] = (byte) ((bits >> 24) & 0x000000FF);
		return bytes;
	}

	public static byte[] doubleToByteArray(Double sample) {
		return longToByteArray(Double.doubleToLongBits(sample));
	}

	public static byte[] longToByteArray(long sample) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (sample & 0x00FF);
		bytes[1] = (byte) ((sample >> 8) & 0x000000FF);
		bytes[2] = (byte) ((sample >> 16) & 0x000000FF);
		bytes[3] = (byte) ((sample >> 24) & 0x000000FF);
		bytes[4] = (byte) ((sample >> 32) & 0x000000FF);
		bytes[5] = (byte) ((sample >> 40) & 0x000000FF);
		bytes[6] = (byte) ((sample >> 48) & 0x000000FF);
		bytes[7] = (byte) ((sample >> 56) & 0x000000FF);
		return bytes;
	}
}
