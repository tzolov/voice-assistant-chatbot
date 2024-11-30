package spring.ai.demo.ai.marvin;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * ChatBot Assistant Application that uses voice input and output to communicate with the
 * user. The application uses the simple {@link Audio} utility to record and playback the
 * audio.
 *
 * @author Christian Tzolov
 */
@SuppressWarnings("null")
@SpringBootApplication
public class VoiceAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoiceAssistantApplication.class, args);
	}

	@Bean
	public CommandLineRunner chatBot(ChatClient.Builder chatClientBuilder,
			@Value("${audio.chatbot.prompt:classpath:/marvin.paranoid.android.txt}") Resource systemPrompt) {
		return args -> {
			// 1. Audio recording and playback utility, using plain Java Sound API
			Audio audio = new Audio();

			// 2. Create the ChatClient with with system prompt and conversation memory.
			// The application.properties defines the model and the output audio format.
			var chatClient = chatClientBuilder
				.defaultSystem(systemPrompt.getContentAsString(java.nio.charset.Charset.defaultCharset()))
				.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
				.build();

			// 3. Start the chat loop
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					audio.startRecording(); // Start recording the user's voice input
					System.out.print("\nRecording your question ... press Enter to stop! ");
					scanner.nextLine();
					audio.stopRecording(); // Stop recording the user's voice input

					System.out.print("PROCESSING ...\n");

					// Send the user's voice input to the chat client and get the response
					var response = chatClient.prompt()
						.messages(new UserMessage("Please answer the questions in the audio input",
								new Media(MediaType.parseMediaType("audio/wav"),
										new ByteArrayResource(audio.getLastRecording()))))
						.call().chatResponse().getResult().getOutput();

					// Print the response and play the audio response
					System.out.println("\nASSISTANT: " + response.getContent());
					// Play the audio response
					audio.playRecording(response.getMedia().get(0).getDataAsByteArray());
				}
			}
		};
	}

}
