package me.imgalvin;

import java.util.ArrayList;
import java.util.List;

public final class AutoLanPublisherTest {
    public static void main(String[] args) {
        configuredPortFailureRetriesOnceWithFallbackPort();
    }

    private static void configuredPortFailureRetriesOnceWithFallbackPort() {
        List<Integer> attempts = new ArrayList<>();

        AutoLanPublisher.PublishResult result = AutoLanPublisher.publishWithFallback(
                new AutoLanPortResolver.PortSelection(25565, true),
                () -> 34567,
                port -> {
                    attempts.add(port);
                    return port == 34567;
                });

        assertEquals(34567, result.port(), "published port");
        assertTrue(result.success(), "fallback publish should succeed");
        assertTrue(result.fallbackUsed(), "fallback should be marked used");
        assertEquals(2, attempts.size(), "attempt count");
        assertEquals(25565, attempts.get(0), "first attempt");
        assertEquals(34567, attempts.get(1), "fallback attempt");
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
}
