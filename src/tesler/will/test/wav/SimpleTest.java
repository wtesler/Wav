package tesler.will.test.wav;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import tesler.will.wav.Wav;
import tesler.will.wav.Wav.AudioFormatException;
import tesler.will.wav.Wav.MalformedWavFileException;

public class SimpleTest {

	@Test
	public void testProperFile() {

		File file = new File("res/cat.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (MalformedWavFileException e) {
			System.err.println(e.getMessage());
			fail();
		} catch (AudioFormatException e) {
			System.err.println(e.getMessage());
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testNonexistentFile() {

		File file = new File("res/nonexistent.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
		} catch (FileNotFoundException e) {
			// Success
			return;
		} catch (MalformedWavFileException e) {
			fail();
		} catch (AudioFormatException e) {
			fail();
		} catch (IOException e) {
			fail();
		}

		fail();
	}

	@Test
	public void testMalformedWavFile() {

		File file = new File("res/malformed.wav");

		Wav wav = new Wav();

		try {
			wav.readFile(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (MalformedWavFileException e) {
			// Success
			System.err.println(e.getMessage());
			return;
		} catch (AudioFormatException e) {
			fail();
		} catch (IOException e) {
			fail();
		}

		fail();
	}

}
