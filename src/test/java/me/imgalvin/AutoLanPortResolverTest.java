package me.imgalvin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public final class AutoLanPortResolverTest {
    public static void main(String[] args) throws Exception {
        systemPropertyTakesPrecedenceOverConfigFile();
        configFileIsUsedWhenSystemPropertyIsAbsent();
        invalidSystemPropertyFallsBackToConfigFile();
        portsMustBeWithinValidRange();
        invalidConfiguredValueEmitsWarningAndFallsBack();
    }

    private static void systemPropertyTakesPrecedenceOverConfigFile() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("auto-lan.port", "25565");

        Path configFile = Files.createTempFile("auto-lan", ".properties");
        Files.writeString(configFile, "port=24454\n");

        AtomicInteger fallbackCalls = new AtomicInteger();
        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                configFile,
                () -> {
                    fallbackCalls.incrementAndGet();
                    return 34567;
                });

        assertEquals(25565, selection.port(), "system property port");
        assertTrue(selection.configured(), "system property should be marked configured");
        assertEquals(0, fallbackCalls.get(), "fallback should not be called");
    }

    private static void configFileIsUsedWhenSystemPropertyIsAbsent() throws Exception {
        Properties properties = new Properties();

        Path configFile = Files.createTempFile("auto-lan", ".properties");
        Files.writeString(configFile, "port=24454\n");

        AtomicInteger fallbackCalls = new AtomicInteger();
        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                configFile,
                () -> {
                    fallbackCalls.incrementAndGet();
                    return 34567;
                });

        assertEquals(24454, selection.port(), "config file port");
        assertTrue(selection.configured(), "config file should be marked configured");
        assertEquals(0, fallbackCalls.get(), "fallback should not be called");
    }

    private static void invalidSystemPropertyFallsBackToConfigFile() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("auto-lan.port", "not-a-port");

        Path configFile = Files.createTempFile("auto-lan", ".properties");
        Files.writeString(configFile, "port=24454\n");

        AtomicInteger fallbackCalls = new AtomicInteger();
        List<String> warnings = new ArrayList<>();
        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                configFile,
                () -> {
                    fallbackCalls.incrementAndGet();
                    return 34567;
                },
                warnings::add);

        assertEquals(24454, selection.port(), "config file port after invalid system property");
        assertTrue(selection.configured(), "config file should be marked configured");
        assertEquals(0, fallbackCalls.get(), "fallback should not be called");
        assertEquals(1, warnings.size(), "warning count");
    }

    private static void portsMustBeWithinValidRange() throws Exception {
        assertResolvedSystemPort("1", 1);
        assertResolvedSystemPort("65535", 65535);
        assertFallsBackForSystemPort("0");
        assertFallsBackForSystemPort("65536");
    }

    private static void assertResolvedSystemPort(String configuredValue, int expectedPort) {
        Properties properties = new Properties();
        properties.setProperty("auto-lan.port", configuredValue);

        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                Path.of("missing.properties"),
                () -> 34567,
                warning -> {
                });

        assertEquals(expectedPort, selection.port(), "system property boundary port");
        assertTrue(selection.configured(), "boundary port should be marked configured");
    }

    private static void assertFallsBackForSystemPort(String configuredValue) {
        Properties properties = new Properties();
        properties.setProperty("auto-lan.port", configuredValue);

        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                Path.of("missing.properties"),
                () -> 34567,
                warning -> {
                });

        assertEquals(34567, selection.port(), "fallback port for invalid boundary");
        assertFalse(selection.configured(), "invalid boundary should not be marked configured");
    }

    private static void invalidConfiguredValueEmitsWarningAndFallsBack() {
        Properties properties = new Properties();
        properties.setProperty("auto-lan.port", "65536");
        List<String> warnings = new ArrayList<>();

        AutoLanPortResolver.PortSelection selection = AutoLanPortResolver.resolve(
                properties,
                Path.of("missing.properties"),
                () -> 34567,
                warnings::add);

        assertEquals(34567, selection.port(), "fallback port after warning");
        assertFalse(selection.configured(), "invalid value should not be marked configured");
        assertEquals(1, warnings.size(), "warning count");
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) {
            throw new AssertionError(label);
        }
    }
}
