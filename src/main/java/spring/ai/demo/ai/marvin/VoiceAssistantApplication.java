package spring.ai.demo.ai.marvin;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
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
			@Value("${chatbot.prompt:classpath:/marvin.paranoid.android.txt}") Resource systemPrompt) {
		return args -> {

			// Create the ChatClient with with system prompt and conversation memory.
			// The model and the audio formats are configured in the application.properties file.
			var chatClient = chatClientBuilder
				.defaultSystem(systemPrompt)
				.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
				.build();

			try (Scanner scanner = new Scanner(System.in)) {

				// Audio utility to record and playback the audio.
				Audio audio = new Audio();

				// Start the chat loop
				while (true) {					
					//Record user's voice input
					audio.startRecording(); 
					System.out.print("\nRecording your question ... press <Enter> to stop! ");
					scanner.nextLine();
					audio.stopRecording();

					System.out.print("PROCESSING ...\n");

					// Send user's input to the AI model and get the response
					AssistantMessage response = chatClient.prompt()
						.messages(new UserMessage("Please answer the questions in the audio input",
								new Media(MediaType.parseMediaType("audio/wav"),
										new ByteArrayResource(audio.getLastRecording()))))
						.call()
						.chatResponse()
						.getResult()
						.getOutput();

					// Print the text (e.g. transcription) response
					System.out.println("\nASSISTANT: " + response.getText());
					// Play the audio response
					Audio.play(response.getMedia().get(0).getDataAsByteArray());
				}
			}
		};
	}

}
