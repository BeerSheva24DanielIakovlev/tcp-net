package telran.net;

import java.util.concurrent.atomic.AtomicInteger;

public class DosProtection {
    private final AtomicInteger notOkResponses = new AtomicInteger(0);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private volatile long lastRequestTimeMillis = System.currentTimeMillis();

    public boolean isRateLimitExceeded() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastRequestTimeMillis > 1000) {
            requestCount.set(0);
            lastRequestTimeMillis = currentTimeMillis;
        }
        return requestCount.incrementAndGet() > TcpConfigurationProperties.MAX_REQUESTS_PER_SECOND;
    }

    public boolean isNotOkLimitExceeded(ResponseCode responseCode) {
        if (responseCode != ResponseCode.OK) {
            notOkResponses.incrementAndGet();
        }
        return notOkResponses.get() > TcpConfigurationProperties.MAX_NOT_OK_RESPONSES;
    }
}
