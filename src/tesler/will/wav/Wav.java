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

	// Riff Chunk Variables
	String riffChunkId;
	int riffChunkSize;
	String riffFormat;

	// FMT Chunk Variables
	String fmtChunkId;
	int fmtChunkSize;
	int fmtAudioFormat;
	int fmtChannels;
	int fmtSampleRate;
	int fmtByteRate;
	short fmtBlockAlign;
	short fmtBitsPerSample;

	// Data Chunk Variables
	String dataChunkId;
	long dataChunkSize;

	// Holds the actual byte data
	byte[] byteData;

	// Only set if fmtBitsPerSample == 16
	short[] shortData;

	// Only set if fmtBitsPerSample == 32
	int[] intData;

	public Wav() {};

	public void readFile(File file) throws IOException, FileNotFoundException,
			MalformedWavFileException, AudioFormatException {

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
			riffChunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_RIFF.equals(riffChunkId)) {
				throw new MalformedWavFileException("bytes 0-3 of file did not contain \"Riff\"",
						MalformedWavFileException.ERROR_RIFF);
			}

			inStream.read(tmpInt);
			riffChunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpString);
			riffFormat = ByteUtils.byteArrayToString(tmpString);
			if (!ID_WAVE.equals(riffFormat)) {
				throw new MalformedWavFileException("bytes 5-8 of file did not contain \"WAVE\"",
						MalformedWavFileException.ERROR_WAVE);
			}

			// Read the FMT chunk.

			inStream.read(tmpString);
			fmtChunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_FMT.equals(fmtChunkId)) {
				throw new MalformedWavFileException("bytes 9-12 of file did not contain \"fmt \"",
						MalformedWavFileException.ERROR_FMT);
			}

			inStream.read(tmpInt);
			fmtChunkSize = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			fmtAudioFormat = ByteUtils.byteArrayToShort(tmpShort);
			if (fmtAudioFormat != FORMAT_PCM) {
				throw new AudioFormatException("Cannot handle compressed audio at this time.",
						AudioFormatException.ERROR_COMPRESSED_AUDIO);
			}

			inStream.read(tmpShort);
			fmtChannels = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpInt);
			fmtSampleRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpInt);
			fmtByteRate = ByteUtils.byteArrayToInt(tmpInt);

			inStream.read(tmpShort);
			fmtBlockAlign = ByteUtils.byteArrayToShort(tmpShort);

			inStream.read(tmpShort);
			fmtBitsPerSample = ByteUtils.byteArrayToShort(tmpShort);

			// Read the Data chunk.

			inStream.read(tmpString);
			dataChunkId = ByteUtils.byteArrayToString(tmpString);
			if (!ID_DATA.equals(dataChunkId)) {
				throw new MalformedWavFileException("bytes 36-39 did not contain " + ID_DATA,
						MalformedWavFileException.ERROR_DATA);
			}

			inStream.read(tmpLong);
			dataChunkSize = ByteUtils.byteArrayToLong(tmpLong);

			byteData = new byte[(int)dataChunkSize];

			inStream.read(byteData);

			// Convert byte data to short or int (if necessary).

			if (fmtBitsPerSample == 16) {
				// the data needs to be interpreted as shorts
				shortData = new short[byteData.length / 2];
				ByteBuffer.wrap(byteData)
					.order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(shortData);
			} else if (fmtBitsPerSample == 32) {
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
		stream.println("___" + this.riffChunkId + "___");
		stream.println("Chunk size: " + this.riffChunkSize);
		stream.println("Format: " + this.riffFormat);

		// Print FMT Header
		stream.println("___" + this.fmtChunkId + "___");
		stream.println("Chunk size: " + this.fmtChunkSize);
		stream.println("Audio format: " + this.fmtAudioFormat);
		stream.println("Channels: " + this.fmtChannels);
		stream.println("Sample rate: " + this.fmtSampleRate);
		stream.println("Byte rate: " + this.fmtByteRate);
		stream.println("Block align: " + this.fmtBlockAlign);
		stream.println("Bits per sample: " + this.fmtBitsPerSample);

		// Print Data Header
		stream.println("___" + this.dataChunkId + "___");
		stream.println("Data size: " + this.dataChunkSize);
	}

	class WavException extends Exception {

		private static final long serialVersionUID = 2321000382825261225L;

		protected String errorMsg;
		protected int errorCode;

		public WavException(String errorMsg, int errorCode) {
			this.errorMsg = errorMsg;
			this.errorCode = errorCode;
		}

		public void printErrorMessage() {
			System.err.println(errorMsg);
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
