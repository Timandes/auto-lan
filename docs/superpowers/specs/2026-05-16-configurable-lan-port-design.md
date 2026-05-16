# Configurable LAN Port Design

## Context

AutoLan currently opens a singleplayer world to LAN automatically after the world loads. The selected LAN port is always produced by `HttpUtil.getAvailablePort()` inside `PublishLANServerMixin`, so users cannot choose a stable port.

The requested change is to let users influence the LAN port through a JVM startup parameter or a simple configuration file, while preserving the existing random-port behavior as the fallback.

## Goals

- Support a JVM system property override: `-Dauto-lan.port=25565`.
- Support a simple properties file: `config/auto-lan.properties`.
- Keep random available-port selection as the fallback.
- Avoid adding third-party dependencies for this narrow feature.
- Keep configuration parsing outside the Mixin so the Mixin remains focused on Minecraft behavior.

## Non-Goals

- No in-game settings screen.
- No JSON, TOML, or third-party configuration library.
- No automatic creation of the configuration file is required for the first version.
- No persistent writing of the resolved port.

## Configuration Contract

The configuration file is located at:

```text
config/auto-lan.properties
```

The only supported property is:

```properties
port=25565
```

The valid port range is `1` through `65535`. Empty values, non-numeric values, `0`, negative numbers, and values above `65535` are invalid.

## Resolution Order

Port resolution uses this fixed priority:

1. JVM system property: `auto-lan.port`
2. Properties file key: `port`
3. Random available port from `HttpUtil.getAvailablePort()`

If a higher-priority source is missing, the resolver silently continues to the next source because absence is normal. If a configured value is present but invalid, the resolver logs a warning and continues to the next source. If all configured sources are missing or invalid, it uses the existing random-port behavior.

## Runtime Behavior

The LAN publishing flow should be:

1. Wait until the Minecraft client connection is ready, as it does today.
2. Resolve the preferred port using the resolution order.
3. Call `publishServer(GameType.CREATIVE, true, resolvedPort)`.
4. If the call succeeds, broadcast and log the actual port.
5. If a configured port was selected and `publishServer` fails, log a warning, get a random available port, and retry once.
6. If the retry succeeds, broadcast and log the fallback port.
7. If both attempts fail, broadcast the existing failure message and log the failed ports.

If the resolver directly selected the random fallback, no second retry is needed because the first attempt already used fallback behavior.

## Code Structure

Add a small resolver class:

```text
src/main/java/me/imgalvin/AutoLanPortResolver.java
```

Responsibilities:

- Read `System.getProperty("auto-lan.port")`.
- Read `config/auto-lan.properties` using JDK `Properties`.
- Parse and validate integer port values.
- Return both the selected port and whether it came from a configured source.
- Log invalid configuration values without throwing.

Because this is a new Java class, add class JavaDoc with `@author GPT-5.5`.

Update `PublishLANServerMixin` to call the resolver and handle the retry policy. The Mixin should not contain file parsing or property parsing logic.

## Error Handling

- Missing `config/auto-lan.properties`: debug-level or no log; this is normal.
- Unreadable config file: warning, then continue to random fallback.
- Invalid configured port: warning, then continue to the next source.
- Configured port unavailable: warning, then retry once with `HttpUtil.getAvailablePort()`.

The user-facing chat message should always show the actual opened LAN port on success.

## Testing

Add focused unit coverage for the resolver if the current build setup can support it without adding new runtime dependencies. If adding test infrastructure would require broader build changes, keep the implementation testable and rely on the Gradle build plus manual checks for this iteration. At minimum, resolver coverage should include:

- System property takes precedence over file configuration.
- File configuration is used when the system property is absent.
- Invalid values fall back to the next source.
- Port boundary validation accepts `1` and `65535`, rejects `0` and `65536`.

Also run the Gradle build after implementation:

```bash
./gradlew build
```

## Documentation

Update the README usage section to mention:

- JVM override: `-Dauto-lan.port=25565`
- Config file: `config/auto-lan.properties`
- Random port fallback remains active when no valid configured port is supplied.
