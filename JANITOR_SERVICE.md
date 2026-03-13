# Janitor Service Implementation

## Overview
The Janitor service is a background process that monitors and cleans up messages stuck in a `PICKED` state beyond a configurable timeout duration. It marks such hung/abandoned messages with a `DEAD` status and unsets the `PICKED` flag to allow potential recovery or further analysis.

## Design Principles
1. **Framework-Level Cleanup**: Operates on framework status (PICKED/DEAD), not business domain statuses
2. **Topic-Agnostic**: Scans all messages across all topics
3. **Non-Blocking**: Crashed task processors don't block the queue; the Janitor handles cleanup independently
4. **Configurable**: Timeout duration is configurable to accommodate both short and long-running tasks

## Components Implemented

### 1. Configuration (`core/src/main/scala/org/example/Configuration.scala`)
- **New Config**: `hungJobTimeoutMinutes` (default: 30 minutes)
- Configurable via: `janitor.hung-job-timeout-minutes` property
- Defines the threshold beyond which a PICKED message is considered hung

### 2. JanitorRepository Interface (`dao/src/main/scala/org/example/JanitorRepository.scala`)
Defines two operations:

```scala
trait JanitorRepository {
  def fetchHungMessages(cutoffTime: Instant): Publisher[Message]
  def markMessageAsDead(_id: String): Publisher[Message]
}
```

- **fetchHungMessages**: Queries MongoDB for messages with `PICKED` field set but `pickedAt` before cutoff time
- **markMessageAsDead**: Updates a message with `DEAD` status and unsets the `PICKED` flag

### 3. JanitorRepositoryUsingMongo (`dao/src/main/scala/org/example/JanitorRepositoryUsingMongo.scala`)
MongoDB implementation using:
- `Filters.exists("PICKED", exists = true)`: Find messages with PICKED flag
- `Filters.lt("pickedAt", cutoffTime)`: Find messages picked before timeout threshold
- `Updates.set("status", "DEAD")`: Mark message as DEAD
- `Updates.unset("PICKED")`: Remove PICKED flag to prevent reprocessing

### 4. JanitorService (`services/src/main/scala/org/example/JanitorService.scala`)
Business logic service that:
- Calculates cutoff time based on `hungJobTimeout` duration
- Fetches hung messages from the repository
- Marks each hung message as DEAD
- Provides logging for audit trail
- Returns count of messages cleaned up

### 5. Janitor Executor (`workshop/src/main/scala/org/example/Janitor.scala`)
Standalone application that:
- Runs as a background service (can be started independently)
- Uses Pekko Streams to schedule periodic cleanup cycles
- Initial delay: 10 seconds
- Polling interval: 5 minutes
- Logs cleanup results

## Message Lifecycle

```
PICKED state exists: pickedAt timestamp is set
↓
Time passes...
↓
Janitor scans: now - pickedAt > hungJobTimeoutMinutes?
↓
YES → Mark as DEAD, unset PICKED
NO → Leave alone (either recently picked or legitimately long-running)
```

## States in Detail

### 1. Recently Picked (LEGITIMATE)
- Message in PICKED state for < grace period
- **Action**: Leave alone
- **Reason**: Actively being processed

### 2. Long-Running Jobs (LEGITIMATE)
- Message in PICKED state < hungJobTimeoutMinutes
- **Action**: Leave alone
- **Reason**: Legitimately taking longer (e.g., batch processing)

### 3. Hung/Abandoned Jobs (CLEANUP)
- Message in PICKED state > hungJobTimeoutMinutes
- **Action**: Mark as DEAD, unset PICKED
- **Reason**: Likely crashed/abandoned; prevented subsequent polling

## Integration with Existing System

### Dependencies Added
- `workshop` module now depends on `services` module

### No Changes to
- Minion executor
- Queue management
- Task execution
- API endpoints

## Running the Janitor Service

```bash
cd /home/justc/ws/minions
./gradlew workshop:run --args='org.example.Janitor'
```

Or build and run the distribution:
```bash
cd workshop/build/distributions
tar -xzf workshop-1.0-SNAPSHOT.tar.gz
./workshop-1.0-SNAPSHOT/bin/workshop
```

## Configuration Example

In your application properties (when implemented):
```properties
janitor.hung-job-timeout-minutes=30
```

## Logging

The Janitor service provides comprehensive logging:
- `INFO`: Startup, scan cycles, results
- `WARN`: Messages marked as DEAD
- `ERROR`: Exception handling during cleanup

Example log output:
```
INFO Janitor service starting...
INFO Janitor configuration: hungJobTimeout=30 minutes, pollInterval=5 minutes
INFO Janitor starting cleanup cycle...
INFO Janitor found 3 hung messages
WARN Marking message 60f... as DEAD - picked at 2026-03-14T08:00:00Z (cutoff: 2026-03-14T07:30:00Z)
INFO Janitor cleanup cycle completed. Marked 3 messages as DEAD
```

## Future Enhancements

1. Make Janitor polling interval configurable
2. Add metrics/monitoring for hung message counts
3. Implement message recovery/retry logic for DEAD messages
4. Add API endpoints to query/manage DEAD messages
5. Implement configurable grace period before marking as DEAD
6. Add Janitor status endpoint (last run, messages processed, etc.)

## Testing Considerations

To test the Janitor service:
1. Create a message and manually set PICKED flag
2. Set pickedAt to a time > hungJobTimeoutMinutes ago
3. Run Janitor cleanup
4. Verify message status changed to DEAD and PICKED flag removed

