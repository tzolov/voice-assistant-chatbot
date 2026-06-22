# Voice Assistant ChatBot

This is a Spring Boot application that serves as a voice-enabled chatbot assistant. 
It uses [Spring AI](https://docs.spring.io/spring-ai/reference/index.html) to integrate with OpenAI and to leverages its [audio generation](https://platform.openai.com/docs/guides/audio) features to process voice inputs, and respond with audio outputs. 
The application uses, plain, Java's Sound API for audio recording and playback.

The assistant can also reach out to the web: it is equipped with web search and web fetch tools, 
so it can look up and read up-to-date information when answering your spoken questions.

By default, the assistant impersonates Marvin, using the [marvin.paranoid.android.txt](https://github.com/tzolov/voice-assistant-chatbot/blob/main/src/main/resources/marvin.paranoid.android.txt) system prompt.

<img src="doc/marvin-transparent.svg" width="200" align="center"/>

> Marvin - a Paranoid Android, the highly intelligent yet perpetually depressed and pessimistic robot in the Universe. 
> With a brain the size of a planet but endlessly underwhelmed and irritated by the menial tasks given to him... 

Use the `chatbot.prompt=<file-name.txt>` property to configure a different personality.
For example the `chatbot.prompt=classpath:/psychoanalyst.txt`, will set the [psychoanalyst.txt](https://github.com/tzolov/voice-assistant-chatbot/blob/main/src/main/resources/psychoanalyst.txt) prompt to impersonate a psychoanalyst.

## Features

- **Voice Input and Output**: Communicates using recorded voice input and generates audio responses.
- **Web Search and Fetch Tools**: Equipped with a Brave-powered web search tool (`BraveWebSearchTool`) and a content fetch tool (`SmartWebFetchTool`) from [spring-ai-agent-utils](https://github.com/spring-ai-community/spring-ai-agent-utils), so the assistant can retrieve and read live information from the web.
- **Session Memory with Compaction**: Maintains conversation context using the session-based `SessionMemoryAdvisor` from [spring-ai-session](https://github.com/spring-ai-community/spring-ai-session), with automatic history compaction (a sliding-window strategy triggered by turn count).
- **Request/Response Logging**: A custom `MyLoggingAdvisor` prints the user input, available tools, tool calls, conversation memory, and assistant responses to the console.
- **System Prompt**: Configurable system prompt to define the chatbot's behavior.
- **Spring AI Integration**: Utilizes Spring AI's [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html) to interact with a chat model.

## Requirements

- **Java**: Java 17 or higher.
- **Spring Boot**: Version 4.0.x or higher.
- **Dependencies**: `spring-ai-starter-model-openai` (version `2.0.0` or higher), `spring-ai-agent-utils` (web search/fetch tools), and `spring-ai-session` (session memory). On Apple-silicon macOS the `netty-resolver-dns-native-macos` (`osx-aarch_64`) dependency is also included for native DNS resolution.
- **OpenAI API Key**: Follow the Spring AI OpenAI integration [instruction](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html) to configure your access to OpenAI. Provide it via the `OPENAI_API_KEY` environment variable.
- **Brave Search API Key**: The web search tool uses the [Brave Search API](https://brave.com/search/api/). Provide your key via the `BRAVE_API_KEY` environment variable.
- **OpenAI Model**: An audio-capable model is required for the input/output audio modality (e.g. `gpt-audio-mini-2025-12-15`, `gpt-audio`, or the older `gpt-4o-audio-preview`).
- **Microphone access**: The OS must grant microphone access to the process that launches the app (see [Microphone Permission](#microphone-permission-macos) below).

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-repo/assistant-application.git
cd assistant-application
```

### Configuration

```properties
spring.main.web-application-type=none

# System prompt message
chatbot.prompt=classpath:/marvin.paranoid.android.txt

# OpenAI API key
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Set the OpenAI model (must be audio-capable)
spring.ai.openai.chat.model=gpt-audio-mini-2025-12-15
spring.ai.openai.chat.max-tokens=4096

# Output audio configuration
spring.ai.openai.chat.output-modalities=text,audio
spring.ai.openai.chat.output-audio.voice=ONYX
spring.ai.openai.chat.output-audio.format=WAV
```

> The web search tool additionally requires a `BRAVE_API_KEY` environment variable to be set.

### Build the Application

```bash
./mvnw clean install
```

### Run the Application

```bash
java -jar ./target/voice-assistant-chatbot-0.2.1-SNAPSHOT.jar
```

> **macOS note:** launch from a host that has microphone permission (see [Microphone Permission](#microphone-permission-macos)). When running `java -jar ...` directly from a terminal, that terminal app must be granted microphone access.

```
 ▗▄▄▖▗▄▄▖ ▗▄▄▖ ▗▄▄▄▖▗▖  ▗▖ ▗▄▄▖     ▗▄▖ ▗▄▄▄▖                                    
▐▌   ▐▌ ▐▌▐▌ ▐▌  █  ▐▛▚▖▐▌▐▌       ▐▌ ▐▌  █                                      
 ▝▀▚▖▐▛▀▘ ▐▛▀▚▖  █  ▐▌ ▝▜▌▐▌▝▜▌    ▐▛▀▜▌  █                                      
▗▄▄▞▘▐▌   ▐▌ ▐▌▗▄█▄▖▐▌  ▐▌▝▚▄▞▘    ▐▌ ▐▌▗▄█▄▖                                    
▗▄▄▖  ▗▄▖ ▗▄▄▖  ▗▄▖ ▗▖  ▗▖ ▗▄▖ ▗▄▄▄▖▗▄▄▄      ▗▄▖ ▗▖  ▗▖▗▄▄▄ ▗▄▄▖  ▗▄▖ ▗▄▄▄▖▗▄▄▄ 
▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▐▛▚▖▐▌▐▌ ▐▌  █  ▐▌  █    ▐▌ ▐▌▐▛▚▖▐▌▐▌  █▐▌ ▐▌▐▌ ▐▌  █  ▐▌  █
▐▛▀▘ ▐▛▀▜▌▐▛▀▚▖▐▛▀▜▌▐▌ ▝▜▌▐▌ ▐▌  █  ▐▌  █    ▐▛▀▜▌▐▌ ▝▜▌▐▌  █▐▛▀▚▖▐▌ ▐▌  █  ▐▌  █
▐▌   ▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▐▌  ▐▌▝▚▄▞▘▗▄█▄▖▐▙▄▄▀    ▐▌ ▐▌▐▌  ▐▌▐▙▄▄▀▐▌ ▐▌▝▚▄▞▘▗▄█▄▖▐▙▄▄▀
voice-assistant-chatbot:0.2.1-SNAPSHOT/Spring AI:2.0.0/Spring Boot:4.0.7

2024-12-01T11:00:11.274+01:00  INFO 31297 --- [voice-assistant-chatbot] [           main] s.a.d.a.m.VoiceAssistantApplication      : Started VoiceAssistantApplication in 0.827 seconds (process running for 1.054)

Recording your question ... press <Enter> to stop!
```

### Interacting with the Assistant

1. Speak your query when prompted.
2. Press `Enter` to stop recording.
3. Listen to the assistant’s response, which will be played back.

Press `Ctrl+C` to exit.

## Microphone Permission (macOS)

On macOS the microphone permission is granted to the **application that launches the JVM**, not to Java itself:

- Running from a terminal (`Terminal.app`, `iTerm2`) → that terminal app needs the permission.
- Running from an IDE (VS Code, IntelliJ) Run button or integrated terminal → the IDE needs the permission.

If access is **not** granted, macOS does not throw an error — it silently feeds an all-zero (silent) audio stream. The recording succeeds and is sent to the model, but the assistant responds as if it heard nothing.

To fix:

1. Open **System Settings → Privacy & Security → Microphone** and enable the host app.
2. **Fully quit and relaunch** that app — an already-running process (and the JVM it spawned) keeps its original permission decision; toggling the setting does not reach it. Closing only the window is not enough.
3. Confirm the correct input device is selected and not muted under **System Settings → Sound → Input**.

To verify the captured audio actually contains sound, inspect the recording buffer (`AudioRecordBuffer.wav`) — a peak amplitude of `0` across all samples means the stream is silent (still blocked).

## Code Overview

The application consists of two classes the `VoiceAssistantApplication.java` and the utility `Audio`.

The [VoiceAssistantApplication.java](https://github.com/tzolov/voice-assistant-chatbot/blob/main/src/main/java/spring/ai/demo/ai/marvin/VoiceAssistantApplication.java) is the  main class initializes the chatbot with:

1. **ChatClient**: Configures the chatbot with the system prompt, the session-memory advisor (`SessionMemoryAdvisor`, backed by an in-memory session repository with sliding-window compaction), the logging advisor (`MyLoggingAdvisor`), and the web search/fetch tools (`BraveWebSearchTool` and `SmartWebFetchTool`).
2. **Command Line Runner**: Implements a loop to continuously record, process, and respond to user input.
3. **Audio Recording and Playback**: Manages voice input and output using the [Audio](https://github.com/tzolov/voice-assistant-chatbot/blob/main/src/main/java/spring/ai/demo/ai/marvin/Audio.java) utility for recording audio input from the user and playing back audio responses.
It is a single class implementation, leveraging the pain `Java Sound API` for capturing and playback audio. 

The [MyLoggingAdvisor.java](https://github.com/tzolov/voice-assistant-chatbot/blob/main/src/main/java/spring/ai/demo/ai/marvin/MyLoggingAdvisor.java) is a custom `BaseAdvisor` that prints the user input, available tools, tool calls, tool responses, conversation memory, and assistant responses to the console for easy inspection of what the model receives and returns.

The `SmartWebFetchTool` is wired with its own cloned `ChatClient` (without the session-memory advisor) so that its internal model calls do not pollute the main conversation history.
