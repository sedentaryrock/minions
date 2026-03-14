# Minion - Distributed Resilient Job Framework

A Scala-based distributed job processing framework built on **Pekko Streams** and **MongoDB**, designed for reliable, scalable task execution across multiple workers.

## Overview

Minion enables you to define and execute distributed jobs with built-in resilience features. Messages flow through configurable task pipelines with persistent state management, ensuring no work is lost even during failures.

## Key Features

- **Stream-Based Processing**: Leverages Pekko Streams for efficient, non-blocking message handling
- **Persistent State**: MongoDB-backed message and transition management for durability
- **Configurable Pipelines**: Define task flows with arbitrary status transitions
- **Distributed Execution**: Multiple instances can safely process messages concurrently
- **Dead Letter Handling**: Framework-level status management for failed or unprocessable messages
- **Flexible Task Model**: Generic task interface supporting any serializable computation

## Architecture

The framework implements a pickup → execute → update → putdown pipeline:

1. **Pickup**: Poll for messages in a specific topic/status
2. **Execute**: Run the configured task on each message
3. **Update**: Persist task results
4. **Putdown**: Transition message to new status (or DEAD if failed)

## Technology Stack

- **Language**: Scala
- **Streaming**: Apache Pekko Streams
- **Persistence**: MongoDB
- **Build**: Gradle

## Getting Started

[Add setup, configuration, and usage examples here]

## Contributing

[Add contribution guidelines]