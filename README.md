# Voice Assistant ChatBot

This is a Spring Boot application that serves as a voice-enabled chatbot assistant. 
It leverages Spring AI to process voice inputs, interact with an OpenAI chat model, and respond with audio outputs. 
The application uses Java's Sound API for audio recording and playback.

By default, it uses a system prompt that impersonates Marvin.

<img src="doc/marvin-transparent.svg" width="200" align="center"/>

> Marvin - a Paranoid Android, the highly intelligent yet perpetually depressed and pessimistic robot in the Universe. 
> With a brain the size of a planet but endlessly underwhelmed and irritated by the menial tasks given to him... 

## Features

- **Voice Input and Output**: Communicates using recorded voice input and generates audio responses.
- **Chat Memory**: Maintains context using in-memory chat memory.
- **System Prompt**: Configurable system prompt to define the chatbot's behavior.
- **Spring AI Integration**: Utilizes Spring AI's `ChatClient` to interact with a chat model.

## Requirements

- **Java**: Java 17 or higher.
- **Spring Boot**: Version 3.2.x or higher.
- **Dependencies**:
  - `spring-ai-openai-spring-boot-starter`
  - Other transitive dependencies managed by Spring Boot.

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-repo/assistant-application.git
cd assistant-application
```

### Configuration

#### Application Properties

```properties
spring.main.web-application-type=none

# System prompt message
audio.chatbot.prompt=classpath:/marvin.paranoid.android.txt

# OpenAI API key
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Set the OpenAI model
spring.ai.openai.chat.options.model=gpt-4o-audio-preview

# Output audio configuration
spring.ai.openai.chat.options.output-modalities=text,audio
spring.ai.openai.chat.options.output-audio.voice=ONYX
spring.ai.openai.chat.options.output-audio.format=WAV
```

### Build the Application

```bash
./mvnw clean install
```

### Run the Application

```bash
./mvnw spring-boot:run
```

### Interacting with the Assistant

1. Speak your query when prompted.
2. Press `Enter` to stop recording.
3. Listen to the assistant’s response, which will be played back.

## Code Overview

### `VoiceAssistantApplication`

The main class initializes the chatbot with:

1. **Audio Recording and Playback**: Manages voice input and output using the `Audio` utility.
2. **ChatClient**: Configures the chatbot using the system prompt and an in-memory chat memory advisor.
3. **Command Line Runner**: Implements a loop to continuously record, process, and respond to user input.

### `Audio` Utility

A helper class for:

- Recording audio input from the user using the Java Sound API.
- Playing back audio responses.

The `Audio` class uses a `TargetDataLine` for capturing audio input and a `Clip` for playback. It supports the WAV audio format and stores the recordings in a temporary file (`AudioRecordBuffer.wav`). Note that this implementation is not thread-safe and is intended for demonstration purposes.
