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
 * Warning: This implementation is not thread-safe and should not be used in a production.
 *
 * @author Christian Tzolov
 */
public class Audio {

	// Volatile to ensure visibility across threads
	private volatile AudioFormat format;

	private volatile TargetDataLine microphone;

	/**
	 * The file to store the recorded audio. Plays the role of a buffer.
	 */
	private final File wavFile;

	public Audio() {
		this(new AudioFormat(44100.0f, 16, 1, true, true), "AudioRecordBuffer.wav");
	}

	public Audio(AudioFormat format, String wavFileName) {
		this.format = format;
		this.wavFile = new File(wavFileName);
	}

	public void startRecording() {

		new Thread(() -> {
			try {
				// Ensure clean slate for new recording
				stopRecording();
				this.microphone = AudioSystem.getTargetDataLine(this.format);
				this.microphone.open(this.format);
				this.microphone.start();
				AudioSystem.write(new AudioInputStream(this.microphone), AudioFileFormat.Type.WAVE, this.wavFile);
			}
			catch (Exception e) {
				throw new RuntimeException("Recording failed", e);
			}
		}).start();
	}

	public void stopRecording() {
		if (this.microphone != null) {
			this.microphone.stop();
			this.microphone.close();
			this.microphone = null;
		}
	}

	public byte[] getLastRecording() {
		try {
			return (this.wavFile.exists())
					? StreamUtils.copyToByteArray(new BufferedInputStream(new FileInputStream(this.wavFile)))
					: new byte[0];
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read recording", e);
		}
	}

	public static void play(byte[] waveData) { // java utils to play wav audio
		try (Clip clip = AudioSystem.getClip();
				AudioInputStream audio = AudioSystem
					.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(waveData)));) {
			clip.open(audio);
			clip.start();
			while (!clip.isRunning()) {
				Thread.sleep(1000);
			} // wait to start
			while (clip.isRunning()) {
				Thread.sleep(3000);
			} // wait to finish
			clip.stop();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
