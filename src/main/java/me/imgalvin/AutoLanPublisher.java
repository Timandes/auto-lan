package me.imgalvin;

import java.util.function.IntPredicate;
import java.util.function.IntSupplier;

/**
 * Publishes AutoLan using the resolved port and fallback policy.
 *
 * @author GPT-5.5
 */
public final class AutoLanPublisher {
    private AutoLanPublisher() {
    }

    public static PublishResult publishWithFallback(
            AutoLanPortResolver.PortSelection selection,
            IntSupplier fallbackPort,
            IntPredicate publishPort) {
        if (publishPort.test(selection.port())) {
            return new PublishResult(selection.port(), true, false);
        }

        if (selection.configured()) {
            int port = fallbackPort.getAsInt();
            return new PublishResult(port, publishPort.test(port), true);
        }

        return new PublishResult(selection.port(), false, false);
    }

    public record PublishResult(int port, boolean success, boolean fallbackUsed) {
    }
}
