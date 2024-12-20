# DiscordBotCore

DiscordBotCore is an early version of the codebase that evolved into **The Armory**, a Discord bot framework focused on utility and extensibility.

## Table of Contents

- [About](#about)
- [Features](#features)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Setup](#setup)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## About

This repository contains an early iteration of a Discord bot framework. While functional, it has since been superseded by **The Armory** and is no longer actively developed. The framework demonstrates the foundational ideas and basic functionality that were later expanded and improved.

## Features

- **Command Handler**: Basic infrastructure for handling commands.
- **Event System**: Implements Discord bot events such as message handling and user joins.
- **Extensible Framework**: Designed to allow modular extensions and custom features.
- **Token-Based Authentication**: Requires a bot token to authenticate with the Discord API.

## Installation

### Prerequisites

- **Java Development Kit (JDK)**: Ensure JDK 8 or higher is installed.
- **Maven**: Used for building and managing dependencies.
- **Discord Developer Account**: Set up a bot via the [Discord Developer Portal](https://discord.com/developers/applications).

### Setup

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Horeak/DiscordBotCore.git
   cd DiscordBotCore
   ```

2. **Build the Project**:

   Compile and package the bot using Maven:

   ```bash
   mvn clean package
   ```

   This will create a `.jar` file in the `target` directory.

3. **Configure Environment Variables**:

   Create a `.env` file in the root directory with the following content:

   ```
   DISCORD_TOKEN=your_bot_token_here
   ```

   Replace `your_bot_token_here` with your Discord bot token.

4. **Run the Bot**:

   Execute the compiled JAR file:

   ```bash
   java -jar target/DiscordBotCore-1.0-SNAPSHOT.jar
   ```

## Usage

After starting the bot, use the defined commands (as implemented in the code) to interact with it. This repository serves as a framework and may require additional customization or feature implementation for specific use cases.

## Contributing

As this repository is archived and read-only, contributions are no longer accepted. However, feel free to fork the repository for personal exploration and learning.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
