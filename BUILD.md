# Building for Minecraft 1.21.11

This repository has been configured to build a Minecraft 1.21.11 Fabric jar.

## Build environment

- JDK 21
- Gradle Wrapper from this repository
- Fabric Loader 0.18.4
- Fabric Loom Remap 1.14.10
- Minecraft 1.21.11

Check the active Java runtime:

```bash
java -version
```

The build should use Java 21. The current Gradle configuration also compiles with `options.release = 21`.

## Configuration changes for 1.21.11

Minecraft 1.21.11 is still an obfuscated release, so this project uses the remap Loom plugin:

```gradle
id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
```

The project also uses official Mojang mappings:

```gradle
mappings loom.officialMojangMappings()
```

The version properties are set in `gradle.properties`:

```properties
minecraft_version=1.21.11
loader_version=0.18.4
loom_version=1.14.10
mod_version=1.0.1+1.21.11
```

The mod metadata in `src/main/resources/fabric.mod.json` requires Minecraft 1.21.11 and Java 21:

```json
"depends": {
  "fabricloader": ">=0.18.1",
  "minecraft": "1.21.11",
  "java": ">=21"
}
```

The mixin compatibility level is also set to Java 21:

```json
"compatibilityLevel": "JAVA_21"
```

Fabric API is not required by the current source code, so it is not declared as a build dependency or runtime dependency.

## Network workaround used during the build

The first build may need to download Gradle, Fabric Loom, Minecraft libraries, Fabric Loader, intermediary mappings, and native libraries. Some Fabric Maven downloads can be slow or time out, so Gradle HTTP timeouts were increased:

```properties
systemProp.org.gradle.internal.http.connectionTimeout=300000
systemProp.org.gradle.internal.http.socketTimeout=300000
```

If Fabric Maven is too slow for specific artifacts, place those artifacts in the local project Maven repository at `local-maven/`. The project is configured to check this repository first in both `settings.gradle` and `build.gradle`:

```gradle
maven {
    name = 'Local'
    url = uri('local-maven')
}
```

During the verified build, these Fabric artifacts were downloaded manually with HTTP/1.1 and stored under `local-maven/`:

```bash
mkdir -p local-maven/net/fabricmc/mercury/0.4.3
curl -L --http1.1 --retry 5 --retry-delay 2 --connect-timeout 60 --speed-limit 1024 --speed-time 60 --max-time 900 \
  https://maven.fabricmc.net/net/fabricmc/mercury/0.4.3/mercury-0.4.3.jar \
  -o local-maven/net/fabricmc/mercury/0.4.3/mercury-0.4.3.jar
curl -L --http1.1 --connect-timeout 60 --max-time 120 \
  https://maven.fabricmc.net/net/fabricmc/mercury/0.4.3/mercury-0.4.3.pom \
  -o local-maven/net/fabricmc/mercury/0.4.3/mercury-0.4.3.pom

mkdir -p local-maven/net/fabricmc/intermediary/1.21.11
curl -L --http1.1 --connect-timeout 60 --speed-limit 1024 --speed-time 60 --max-time 600 \
  https://maven.fabricmc.net/net/fabricmc/intermediary/1.21.11/intermediary-1.21.11.pom \
  -o local-maven/net/fabricmc/intermediary/1.21.11/intermediary-1.21.11.pom
curl -L --http1.1 --connect-timeout 60 --speed-limit 1024 --speed-time 60 --max-time 600 \
  https://maven.fabricmc.net/net/fabricmc/intermediary/1.21.11/intermediary-1.21.11-v2.jar \
  -o local-maven/net/fabricmc/intermediary/1.21.11/intermediary-1.21.11-v2.jar
```

`local-maven/` is intentionally ignored by Git because it is only a local dependency cache.

## Build commands

Make sure the Gradle wrapper is executable:

```bash
chmod +x gradlew
```

Run a clean build:

```bash
./gradlew clean build
```

After dependencies are cached, subsequent builds can use:

```bash
./gradlew build
```

The verified output jar is:

```text
build/libs/auto-lan-1.0.1+1.21.11.jar
```

The verified SHA-256 for the generated jar was:

```text
32b7888d6500b2438c7566b49213d261379f2ac1634fc3f08eed6dd1a703775b
```
