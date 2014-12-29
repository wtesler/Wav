package tesler.will.wav;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

	public class RiffDescriptor {
		public String chunkId;
		public int chunkSize;
		public String format;
	}
	public RiffDescriptor riffDescriptor;

	public class FmtDescriptor {
		public String chunkId;
		public int chunkSize;
		public int audioFormat;
		public int channels;
		public int sampleRate;
		public int byteRate;
		public short blockAlign;
		public short bitsPerSample;
	}
	public FmtDescriptor fmtDescriptor;

	public class DataDescriptor {
		public String chunkId;
		public long chunkSize;
	}
	public DataDescriptor dataDescriptor;

	// Holds the actual byte data
	public byte[] byteData;

	// Only set if fmtBitsPerSample == 16
	public short[] shortData;

	// Only set if fmtBitsPerSample == 32
	public int[] intData;

	public Wav() {
		riffDescriptor = new RiffDescriptor();
		fmtDescriptor = new FmtDescriptor();
		dataDescriptor = new DataDescriptor();
	};

	public void readFile(File file) throws IOException, FileNotFoundException,
			MalformedWavFileException, AudioFormatException {

		// Stream that interfaces with the file.
		DataInputStream inStream = null;

		// Temporary storage of byte data from file as it is converted to the proper type.
		byte[] tmpShort = new byte[2];
		byte[] tmpInt = new byte[4];
		byte[] tmpLong = new byte[8];
		byte[] tmpString = new byte[4];

		try {

			// Initialize the stream with the given file.
			inStream = new DataInputStream(new FileInputStream(file));

			// Read the RIFF chunk.

			inStream.read(tmpString);
			riffDescriptor.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_RIFF.equals(riffDescriptor.chunkId)) {
				throw new MalformedWavFileException("bytes 0-3 of file did not contain \"Riff\"",
						MalformedWavFileException.ERROR_RIFF);
			}

			inStream.read(tmpInt);
			riffDescriptor.chunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpString);
			riffDescriptor.format = ByteUtils.byteArrayToString(tmpString);
			if (!ID_WAVE.equals(riffDescriptor.format)) {
				throw new MalformedWavFileException("bytes 5-8 of file did not contain \"WAVE\"",
						MalformedWavFileException.ERROR_WAVE);
			}

			// Read the FMT chunk.

			inStream.read(tmpString);
			fmtDescriptor.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_FMT.equals(fmtDescriptor.chunkId)) {
				throw new MalformedWavFileException("bytes 9-12 of file did not contain \"fmt \"",
						MalformedWavFileException.ERROR_FMT);
			}

			inStream.read(tmpInt);
			fmtDescriptor.chunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			fmtDescriptor.audioFormat = ByteUtils.byteArrayToShort(tmpShort);
			if (fmtDescriptor.audioFormat != FORMAT_PCM) {
				throw new AudioFormatException("Cannot handle compressed audio at this time.",
						AudioFormatException.ERROR_COMPRESSED_AUDIO);
			}

			inStream.read(tmpShort);
			fmtDescriptor.channels = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpInt);
			fmtDescriptor.sampleRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpInt);
			fmtDescriptor.byteRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			fmtDescriptor.blockAlign = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpShort);
			fmtDescriptor.bitsPerSample = ByteUtils.byteArrayToShort(tmpShort);

			// Read the Data chunk.

			inStream.read(tmpString);
			dataDescriptor.chunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_DATA.equals(dataDescriptor.chunkId)) {
				throw new MalformedWavFileException("bytes 36-39 did not contain " + ID_DATA,
						MalformedWavFileException.ERROR_DATA);
			}

			inStream.read(tmpLong);
			dataDescriptor.chunkSize = ByteUtils.byteArrayToLong(tmpLong);

			byteData = new byte[(int)dataDescriptor.chunkSize];

			inStream.read(byteData);

			// Convert byte data to short or int (if necessary).

			if (fmtDescriptor.bitsPerSample == 16) {
				// the data needs to be interpreted as shorts
				shortData = new short[byteData.length / 2];
				ByteBuffer.wrap(byteData)
					.order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(shortData);
			} else if (fmtDescriptor.bitsPerSample == 32) {
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

	public void printHeader(PrintStream stream) {

		// Print RIFF Header
		stream.println("___" + riffDescriptor.chunkId + "___");
		stream.println("Chunk size: " + riffDescriptor.chunkSize);
		stream.println("Format: " + riffDescriptor.format);

		// Print FMT Header
		stream.println("___" + fmtDescriptor.chunkId + "___");
		stream.println("Chunk size: " + fmtDescriptor.chunkSize);
		stream.println("Audio format: " + fmtDescriptor.audioFormat);
		stream.println("Channels: " + fmtDescriptor.channels);
		stream.println("Sample rate: " + fmtDescriptor.sampleRate);
		stream.println("Byte rate: " + fmtDescriptor.byteRate);
		stream.println("Block align: " + fmtDescriptor.blockAlign);
		stream.println("Bits per sample: " + fmtDescriptor.bitsPerSample);

		// Print Data Header
		stream.println("___" + dataDescriptor.chunkId + "___");
		stream.println("Data size: " + dataDescriptor.chunkSize);
	}

	class WavException extends Exception {

		private static final long serialVersionUID = 2321000382825261225L;

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

	public class MalformedWavFileException extends WavException {

		private static final long serialVersionUID = 1579391781964835824L;

		public static final int ERROR_RIFF = -1;
		public static final int ERROR_FMT = -2;
		public static final int ERROR_WAVE = -3;
		public static final int ERROR_DATA = -4;

		public MalformedWavFileException(String errorMsg, int errorCode) {
			super(errorMsg, errorCode);
		}
	}

	public class AudioFormatException extends WavException {

		private static final long serialVersionUID = 1579391781964835824L;

		public static final int ERROR_COMPRESSED_AUDIO = -1;

		public AudioFormatException(String errorMsg, int errorCode) {
			super(errorMsg, errorCode);
		}
	}

}
