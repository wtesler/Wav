package tesler.will.wav;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

/**
 * Processes WAVE files. <br><br>
 *
 * Defined by Stanford's canonical Microsoft WAVE file format.
 *
 * @see <a href="https://ccrma.stanford.edu/courses/422/projects/WaveFormat/">
 * Microsoft WAVE file format</a>
 *
 */
public class Wav {

	// Only format allowed right now.
	// All other formats are considered compressed.
	private final static int FORMAT_PCM = 1;

	// Used to identify different sections of the wave file.
	private final static String ID_RIFF = "RIFF";
	private final static String ID_WAVE = "WAVE";
	private final static String ID_FMT = "fmt ";
	private final static String ID_DATA = "data";

	public class Riff {
		public String chunkId;
		public int chunkSize;
		public String format;
	}
	public Riff riff;

	public class Format {
		public String chunkId;
		public int chunkSize;
		public int audioFormat;
		public int channels;
		public int sampleRate;
		public int byteRate;
		public short blockAlign;
		public short bitsPerSample;
	}
	public Format format;

	public class DataHeader {
		public String chunkId;
		public int chunkSize;
	}
	public DataHeader dataHeader;

	// Holds the actual byte data
	public byte[] byteData;

	// Only set if fmtBitsPerSample == 16
	public short[] shortData;

	// Only set if fmtBitsPerSample == 32
	public int[] intData;

	public Wav() {
		riff = new Riff();
		format = new Format();
		dataHeader = new DataHeader();
	};

	public void readFile(File file) throws IOException, FileNotFoundException, WavException {

		// Stream that interfaces with the file.
		DataInputStream inStream = null;

		// Temporary storage of byte data from file as it is converted to the proper type.
		byte[] tmpShort = new byte[2];
		byte[] tmpInt = new byte[4];
		byte[] tmpString = new byte[4];

		try {

			// Initialize the stream with the given file.
			inStream = new DataInputStream(new FileInputStream(file));

			// Read the RIFF chunk.

			inStream.read(tmpString);
			riff.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_RIFF.equals(riff.chunkId)) {
				throw new WavException("bytes 0-3 of file did not contain \"RIFF\","
						+ "\nInstead, it contained " + riff.chunkId,
						WavException.ERROR_RIFF);
			}

			inStream.read(tmpInt);
			riff.chunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpString);
			riff.format = ByteUtils.byteArrayToString(tmpString);
			if (!ID_WAVE.equals(riff.format)) {
				throw new WavException("bytes 5-8 of file did not contain \"WAVE\","
						+ "\nInstead, it contained " + riff.format,
						WavException.ERROR_WAVE);
			}

			// Read the FMT chunk.

			inStream.read(tmpString);
			format.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_FMT.equals(format.chunkId)) {
				throw new WavException("bytes 9-12 of file did not contain \"fmt \","
						+ "\nInstead, it contained " + format.chunkId,
						WavException.ERROR_FMT);
			}

			inStream.read(tmpInt);
			format.chunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			format.audioFormat = ByteUtils.byteArrayToShort(tmpShort);
			if (format.audioFormat != FORMAT_PCM) {
				throw new WavException("Cannot handle compressed audio at this time.",
						WavException.ERROR_COMPRESSED_AUDIO);
			}

			inStream.read(tmpShort);
			format.channels = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpInt);
			format.sampleRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpInt);
			format.byteRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			format.blockAlign = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpShort);
			format.bitsPerSample = ByteUtils.byteArrayToShort(tmpShort);

			// Read the Data chunk.

			inStream.read(tmpString);
			dataHeader.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_DATA.equals(dataHeader.chunkId)) {
				throw new WavException("bytes 36-39 did not contain " + ID_DATA
						+ "\nInstead, it contained " + dataHeader.chunkId,
						WavException.ERROR_DATA);
			}

			inStream.read(tmpInt);
			dataHeader.chunkSize = ByteUtils.byteArrayToInt(tmpInt);

			byteData = new byte[(int)dataHeader.chunkSize];

			inStream.read(byteData);

			// Convert byte data to short or int (if necessary).

			if (format.bitsPerSample == 16) {
				// the data needs to be interpreted as shorts
				shortData = new short[byteData.length / 2];
				ByteBuffer.wrap(byteData)
					.order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(shortData);
			} else if (format.bitsPerSample == 32) {
				// the data needs to be interpreted as ints
				intData = new int[byteData.length / 4];
				ByteBuffer.wrap(byteData)
					.order(ByteOrder.LITTLE_ENDIAN)
					.asIntBuffer().get(intData);
			}

		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}
	}

	public void writeHeader(DataOutputStream out, int length, AudioFormat format) {

		final int HEADER_SIZE = 36;
		final int SAMPLE_SIZE_IN_BYTES = format.getSampleSizeInBits() / Byte.SIZE;

		try {

			// RIFF CHUNK
			out.writeBytes(ID_RIFF);

			// Total size of wave file in bytes
			out.writeInt(Integer.reverseBytes(HEADER_SIZE + length));

			out.writeBytes(ID_WAVE);

			// FMT CHUNK
			out.writeBytes(ID_FMT);

			// Sample size
			out.writeInt(Integer.reverseBytes(format.getSampleSizeInBits()));

			// Audio format
			out.writeShort(Short.reverseBytes((short) FORMAT_PCM));

			// Number of channels
			out.writeShort(Short.reverseBytes((short) format.getChannels()));

			// Sample rate (hertz)
			out.writeInt(Integer.reverseBytes((int) format.getSampleRate()));

			// Byte rate
			out.writeInt(Integer.reverseBytes((int) (
					format.getSampleRate() * format.getChannels() * SAMPLE_SIZE_IN_BYTES)));

			// Block align
			out.writeShort(Short.reverseBytes((short) (
					format.getChannels() * SAMPLE_SIZE_IN_BYTES)));

			// Bits per sample
			out.writeShort(Short.reverseBytes((short) format.getSampleSizeInBits()));

			// DATA CHUNK
			out.writeBytes(ID_DATA);

			// Data size
			out.writeInt(Integer.reverseBytes(length));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param stream the print stream in which you want to place the generated header.
	 * Some examples include System.out and null.
	 * @return The String which was printed (in case you want to use it later).
	 */
	public String getHeader() {

		StringBuilder builder = new StringBuilder();

		// Print RIFF Header
		builder.append("\n___" + riff.chunkId + "___");
		builder.append("\nChunk size: " + riff.chunkSize);
		builder.append("\nFormat: " + riff.format);

		// Print FMT Header
		builder.append("\n___" + format.chunkId + "___");
		builder.append("\nChunk size: " + format.chunkSize);
		builder.append("\nAudio format: " + format.audioFormat);
		builder.append("\nChannels: " + format.channels);
		builder.append("\nSample rate: " + format.sampleRate);
		builder.append("\nByte rate: " + format.byteRate);
		builder.append("\nBlock align: " + format.blockAlign);
		builder.append("\nBits per sample: " + format.bitsPerSample);

		// Print Data Header
		builder.append("\n___" + dataHeader.chunkId + "___");
		builder.append("\nData size: " + dataHeader.chunkSize);

		return builder.toString();
	}

	public class WavFormat extends AudioFormat {

		public WavFormat(float sampleRate, int sampleSizeInBits, int channels,
				boolean signed, boolean bigEndian) {
			super(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		}

	}

	public class WavException extends Exception {

		private static final long serialVersionUID = 2321000382825261225L;

		// Malformed Wav Errors
		public static final int ERROR_RIFF = -1;
		public static final int ERROR_FMT = -2;
		public static final int ERROR_WAVE = -3;
		public static final int ERROR_DATA = -4;

		// Audio Format errors
		public static final int ERROR_COMPRESSED_AUDIO = -5;

		protected String errorMsg;
		protected int errorCode;

		public WavException(String errorMsg, int errorCode) {
			this.errorMsg = errorMsg;
			this.errorCode = errorCode;
		}

		@Override
		public String getMessage() {
			return errorMsg;
		}

		public int getErrorCode() {
			return errorCode;
		}
	}
}
