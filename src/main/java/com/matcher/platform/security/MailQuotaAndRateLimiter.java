package com.matcher.platform.security;

import com.matcher.platform.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MailQuotaAndRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(MailQuotaAndRateLimiter.class);

    @Value("${app.security.mail.daily-limit:400}")
    private int dailyMailLimit;

    @Value("${app.security.mail.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${app.security.mail.max-requests-per-hour-per-email:3}")
    private int maxRequestsPerHourPerEmail;

    private final AtomicInteger dailyEmailCount = new AtomicInteger(0);
    private volatile LocalDate currentDay = LocalDate.now(ZoneOffset.UTC);

    private final Map<String, Long> lastEmailDispatchTimestamps = new ConcurrentHashMap<>();

    public synchronized void checkAndRecordMailDispatch(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // 1. Reset daily counter if a new UTC day has started
        if (!today.equals(currentDay)) {
            currentDay = today;
            dailyEmailCount.set(0);
            lastEmailDispatchTimestamps.clear();
            log.info("Resetting daily email quota counter for new date: {}", today);
        }

        // 2. Global Daily Quota Check (Prevents Free Gmail SMTP Limit Exhaustion)
        if (dailyEmailCount.get() >= dailyMailLimit) {
            log.warn("Daily SMTP quota exhausted ({}/{}). Rejecting email dispatch to: {}",
                    dailyEmailCount.get(), dailyMailLimit, normalizedEmail);
            throw new BadRequestException("The platform's daily email verification quota has been reached. Please try again tomorrow.");
        }

        // 3. Per-Email Anti-Flood Cooldown Check (60 Seconds minimum between resends)
        Long lastSent = lastEmailDispatchTimestamps.get(normalizedEmail);
        long now = System.currentTimeMillis();
        if (lastSent != null) {
            long elapsedSeconds = (now - lastSent) / 1000;
            if (elapsedSeconds < resendCooldownSeconds) {
                long remaining = resendCooldownSeconds - elapsedSeconds;
                log.warn("Email dispatch rejected for {} due to cooldown ({}s remaining)", normalizedEmail, remaining);
                throw new BadRequestException("Please wait " + remaining + " seconds before requesting another verification code.");
            }
        }

        // 4. Record successful reservation
        int newCount = dailyEmailCount.incrementAndGet();
        lastEmailDispatchTimestamps.put(normalizedEmail, now);
        log.info("Email dispatch quota updated. Today's total: {}/{} | Recipient: {}", newCount, dailyMailLimit, normalizedEmail);

        // Periodically prune in-memory map to prevent memory leak
        if (lastEmailDispatchTimestamps.size() > 10000) {
            long cutoff = now - (3600 * 1000);
            lastEmailDispatchTimestamps.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }
    }

    public int getTodayDispatchCount() {
        return dailyEmailCount.get();
    }

    public int getDailyDispatchCount() {
        return dailyEmailCount.get();
    }

    public int getDailyQuotaLimit() {
        return dailyMailLimit;
    }

    public int getRemainingDailyQuota() {
        return Math.max(0, dailyMailLimit - dailyEmailCount.get());
    }
}
