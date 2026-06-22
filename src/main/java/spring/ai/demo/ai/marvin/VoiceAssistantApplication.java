package spring.ai.demo.ai.marvin;

import java.util.Scanner;

import org.springaicommunity.agent.tools.BraveWebSearchTool;
import org.springaicommunity.agent.tools.SmartWebFetchTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.SlidingWindowCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;

/**
 * ChatBot Assistant Application that uses voice input and output to communicate
 * with the user. The application uses the simple {@link Audio} utility to record and
 * playback the audio.
 *
 * @author Christian Tzolov
 */
@SuppressWarnings("null")
@SpringBootApplication
public class VoiceAssistantApplication {

	private static final MimeType AUDIO_MIME_TYPE = MimeType.valueOf("audio/wav");

	public static void main(String[] args) {
		SpringApplication.run(VoiceAssistantApplication.class, args);
	}

	@Bean
	public CommandLineRunner chatBot(ChatClient.Builder chatClientBuilder,
			@Value("${BRAVE_API_KEY}") String braveApiKey,
			@Value("${chatbot.prompt:classpath:/marvin.paranoid.android.txt}") Resource systemPrompt) {

		return args -> {

			SessionService sessionService = DefaultSessionService.builder().sessionRepository(InMemorySessionRepository.builder().build()).build();

			SessionMemoryAdvisor memoryAdvisor = SessionMemoryAdvisor.builder(sessionService)
				.defaultUserId("alice")
				.compactionTrigger(new TurnCountTrigger(20))
				.compactionStrategy(SlidingWindowCompactionStrategy.builder().maxEvents(10).build())
				.build();

			// Create a clean chat client for the web fetch tool, without the session memory advisor, to avoid sharing the conversation history with the main chat client.
			var webFetchChatClient = chatClientBuilder.clone().build();

			// Create the ChatClient with with system prompt and conversation memory.
			// The model and the audio formats are configured in the application.properties
			// file.
			var chatClient = chatClientBuilder
					.defaultSystem(systemPrompt)
					.defaultAdvisors(a -> a
						.advisors(memoryAdvisor, MyLoggingAdvisor.builder().build())
						.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, "session-123"))
					.defaultTools(
						SmartWebFetchTool.builder(webFetchChatClient).build(),
                    	BraveWebSearchTool.builder(braveApiKey).build())
					.build();

			try (Scanner scanner = new Scanner(System.in)) {

				// Audio utility to record and play back the audio.
				Audio audio = new Audio();

				// Start the chat loop
				while (true) {

					// Record user's voice input
					audio.startRecording();
					System.out.print("\nRecording your question ... press <Enter> to stop! ");
					scanner.nextLine();
					audio.stopRecording();

					System.out.print("PROCESSING ...\n");
					
					// Send user's input to the AI model and get the response
					AssistantMessage response = chatClient.prompt()
							.messages(UserMessage.builder()
									.text("Please answer the questions in the audio input. You can use the provided web search and fetch tools to find the answer if needed.")
									.media(Media.builder().mimeType(AUDIO_MIME_TYPE).data(audio.getLastRecording()).build())
									.build())
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
