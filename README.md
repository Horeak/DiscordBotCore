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

- **Node.js**: Version 16 or higher.
- **Discord Developer Account**: Set up a bot via the [Discord Developer Portal](https://discord.com/developers/applications).

### Setup

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Horeak/DiscordBotCore.git
   cd DiscordBotCore
   ```

2. **Install Dependencies**:

   ```bash
   npm install
   ```

3. **Configure Environment Variables**:

   Create a `.env` file in the root directory with the following content:

   ```
   DISCORD_TOKEN=your_bot_token_here
   ```

   Replace `your_bot_token_here` with your Discord bot token.

4. **Start the Bot**:

   ```bash
   npm start
   ```

## Usage

After starting the bot, use the bot's prefix (as defined in the code) to interact with available commands. Note that this codebase primarily serves as a foundational framework for further development and lacks advanced features.

## Contributing

As this repository is archived and read-only, contributions are no longer accepted. However, feel free to fork the repository for personal exploration and learning.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
