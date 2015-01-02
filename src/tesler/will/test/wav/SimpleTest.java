package tesler.will.test.wav;

import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import org.junit.Assert;

import org.junit.Test;

import tesler.will.wav.Wav;
import tesler.will.wav.Wav.WavException;

public class SimpleTest {

	@Test
	public void testProperFileRead() {

		File file = new File("res/cat.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
			//wav.printHeader(System.out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (WavException e) {
			System.err.println(e.getMessage());
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testNonexistentFileRead() {

		File file = new File("res/nonexistent.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
		} catch (FileNotFoundException e) {
			// Success
			return;
		} catch (WavException e) {
			fail();
		} catch (IOException e) {
			fail();
		}

		fail();
	}

	@Test
	public void testMalformedWavFileRead() {

		File file = new File("res/malformed.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (WavException e) {
			// Success
			if (e.getErrorCode() >= -4 && e.getErrorCode() <= -1) {
				return;
			} else {
				e.printStackTrace();
				fail();
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		fail();
	}

	@Test
	public void testSimpleWavFileWriteHeader() {

		Wav wav = new Wav();

		try {

			File testFile = new File("res/simplewritetest.wav");

			DataOutputStream outFile = new DataOutputStream(
					new FileOutputStream(testFile));

			File catFile = new File("res/cat.wav");

			wav.readFile(catFile);

			String catHeader = wav.getHeader();

			AudioFormat format = new AudioFormat(
					wav.format.sampleRate,
					wav.format.bitsPerSample,
					wav.format.channels,
					true, false);

			wav.writeHeader(outFile, wav.dataHeader.chunkSize, format);

			wav.readFile(testFile);

			String testHeader = wav.getHeader();

			Assert.assertEquals(catHeader, testHeader);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (WavException e) {
			System.err.println(e.getMessage());
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
