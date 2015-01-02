package tesler.will.test.wav;

import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import tesler.will.wav.Wav;
import tesler.will.wav.Wav.WavException;
import tesler.will.wav.Wav.WavFormat;

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

			File catFile = new File("res/cat.wav");

			wav.readFile(catFile);

			String catHeader = wav.getHeader();

			File testFile = new File("res/simplewritetest.wav");

			DataOutputStream outStream = new DataOutputStream(
					new FileOutputStream(testFile));

			wav.writeFile(outStream);

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

	@Test
	public void InitializeWithShortData() {

		Wav wav = new Wav();

		File catFile = new File("res/cat.wav");

		try {

			wav.readFile(catFile);

			WavFormat wavFormat = new WavFormat(wav.format.sampleRate, wav.format.bitsPerSample,
					wav.format.channels, true);

			Wav testWav = new Wav(wav.shortData, wavFormat);

			File testFile = new File("res/initializewithdatatest.wav");

			DataOutputStream outStream = new DataOutputStream(
					new FileOutputStream(testFile));

			testWav.writeFile(outStream);


			String wavHeader = wav.getHeader();
			String testHeader = testWav.getHeader();
			//System.out.println(wavHeader);
			//System.out.println(testHeader);

			Assert.assertEquals(wavHeader, testHeader);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (WavException e) {
			System.err.println(e.getMessage());
			fail();
		}
	}

}
