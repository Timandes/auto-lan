package me.imgalvin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Resolves the LAN port used by AutoLan.
 *
 * @author GPT-5.5
 */
public final class AutoLanPortResolver {
    public static final String SYSTEM_PROPERTY = "auto-lan.port";
    public static final String CONFIG_PROPERTY = "port";
    private static final System.Logger LOGGER = System.getLogger(AutoLanPortResolver.class.getName());

    private AutoLanPortResolver() {
    }

    public static PortSelection resolve(Properties systemProperties, Path configFile, IntSupplier fallbackPort) {
        return resolve(systemProperties, configFile, fallbackPort,
                warning -> LOGGER.log(System.Logger.Level.WARNING, warning));
    }

    static PortSelection resolve(
            Properties systemProperties,
            Path configFile,
            IntSupplier fallbackPort,
            Consumer<String> warningLogger) {
        String configuredPort = systemProperties.getProperty(SYSTEM_PROPERTY);
        OptionalInt systemPort = parsePort(configuredPort, "system property " + SYSTEM_PROPERTY, warningLogger);
        if (systemPort.isPresent()) {
            return new PortSelection(systemPort.getAsInt(), true);
        }

        if (Files.isRegularFile(configFile)) {
            Properties configProperties = new Properties();
            try (InputStream inputStream = Files.newInputStream(configFile)) {
                configProperties.load(inputStream);
            } catch (IOException ignored) {
                return new PortSelection(fallbackPort.getAsInt(), false);
            }

            String filePort = configProperties.getProperty(CONFIG_PROPERTY);
            OptionalInt configPort = parsePort(filePort, "config property " + CONFIG_PROPERTY, warningLogger);
            if (configPort.isPresent()) {
                return new PortSelection(configPort.getAsInt(), true);
            }
        }

        return new PortSelection(fallbackPort.getAsInt(), false);
    }

    private static OptionalInt parsePort(String value, String source, Consumer<String> warningLogger) {
        if (value == null) {
            return OptionalInt.empty();
        }

        if (value.isBlank()) {
            warningLogger.accept("Ignoring blank AutoLan port from " + source + ".");
            return OptionalInt.empty();
        }

        int port;
        try {
            port = Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            warningLogger.accept("Ignoring non-numeric AutoLan port '" + value + "' from " + source + ".");
            return OptionalInt.empty();
        }

        if (port < 1 || port > 65535) {
            warningLogger.accept("Ignoring out-of-range AutoLan port '" + value + "' from " + source + ".");
            return OptionalInt.empty();
        }

        return OptionalInt.of(port);
    }

    public record PortSelection(int port, boolean configured) {
    }
}
