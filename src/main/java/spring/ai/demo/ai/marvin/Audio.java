/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package spring.ai.demo.ai.marvin;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;

import org.springframework.util.StreamUtils;

/**
 * Simple, audio recording and playback utility using plain Java Sound API.
 *
 * It captures computer's Mic for input and Speakers as output.
 *
 * Warning: This implementation is not thread-safe and should not be used in a
 * production.
 *
 * @author Christian Tzolov
 */
public class Audio {

	// Volatile to ensure visibility across threads
	private volatile AudioFormat format;

	private volatile TargetDataLine microphone;

	/**
	 * The thread that streams the microphone capture into the WAV file. Kept so that
	 * {@link #stopRecording()} can wait for the file to be fully flushed and closed
	 * before the recording is read back.
	 */
	private volatile Thread recordingThread;

	/**
	 * The file to store the recorded audio. Plays the role of a buffer.
	 */
	private final File wavFile;

	public Audio() {
		// signed, little-endian PCM. WAV/PCM is little-endian by convention, which is
		// what audio decoders (e.g. OpenAI) expect; big-endian samples decode as noise.
		this(new AudioFormat(44100.0f, 16, 1, true, false), "AudioRecordBuffer.wav");
	}

	public Audio(AudioFormat format, String wavFileName) {
		this.format = format;
		this.wavFile = new File(wavFileName);
	}

	public void startRecording() {

		// Ensure clean slate for new recording
		stopRecording();

		this.recordingThread = new Thread(() -> {
			try {
				this.microphone = AudioSystem.getTargetDataLine(this.format);
				this.microphone.open(this.format);
				this.microphone.start();
				// Blocks until the line is closed by stopRecording(), then flushes and
				// closes the WAV file (updating its header) before returning.
				AudioSystem.write(new AudioInputStream(this.microphone), AudioFileFormat.Type.WAVE, this.wavFile);
			} catch (Exception e) {
				throw new RuntimeException("Recording failed", e);
			}
		});
		this.recordingThread.start();
	}

	public void stopRecording() {
		if (this.microphone != null) {
			this.microphone.stop();
			this.microphone.close();
			this.microphone = null;
		}
		// Wait for the recording thread to finish writing and closing the WAV file so
		// that a subsequent getLastRecording() reads a complete, valid file.
		Thread thread = this.recordingThread;
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.recordingThread = null;
		}
	}

	public byte[] getLastRecording() {
		try {
			return (this.wavFile.exists())
					? StreamUtils.copyToByteArray(new BufferedInputStream(new FileInputStream(this.wavFile)))
					: new byte[0];
		} catch (IOException e) {
			throw new RuntimeException("Failed to read recording", e);
		}
	}

	public static void play(byte[] waveData) { // java utils to play wav audio
		try {
			// Get the audio input stream
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(waveData)));

			// Get the audio format
			AudioFormat format = audioInputStream.getFormat();

			// Create a normalized format that works well with GraalVM
			AudioFormat normalizedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED, // Use signed PCM encoding
					format.getSampleRate(),
					16, // Use 16-bit samples
					format.getChannels(),
					format.getChannels() * 2, // Frame size
					format.getSampleRate(),
					false); // Use little-endian (more widely compatible)

			// Convert to the normalized format
			AudioInputStream normalizedStream = AudioSystem.getAudioInputStream(normalizedFormat, audioInputStream);

			// Create a temporary buffer to ensure all data is valid
			byte[] audioBytes = normalizedStream.readAllBytes();

			// Create a new stream from the validated data
			AudioInputStream validatedStream = new AudioInputStream(
					new ByteArrayInputStream(audioBytes),
					normalizedFormat,
					audioBytes.length / normalizedFormat.getFrameSize());

			// Play the audio
			try (Clip clip = AudioSystem.getClip()) {
				clip.open(validatedStream);
				clip.start();

				// Wait for playback to complete
				while (!clip.isRunning()) {
					Thread.sleep(100);
				}
				while (clip.isRunning()) {
					Thread.sleep(100);
				}

				clip.stop();
			}
		} catch (Exception e) {
			System.err.println("Audio playback error: " + e.getMessage());
			e.printStackTrace();
			// Continue without throwing to prevent application crash
		}
	}

}
