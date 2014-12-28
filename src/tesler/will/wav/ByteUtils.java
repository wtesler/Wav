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

	public static byte[] intToByteArray(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short data) {
		return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
	}
}
