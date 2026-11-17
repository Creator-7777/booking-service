
package com.alena.booking.service;

import com.alena.booking.entity.TelegramLinkToken;
import com.alena.booking.entity.VerifiedCustomer;
import com.alena.booking.repository.TelegramLinkTokenRepository;
import com.alena.booking.repository.VerifiedCustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramLinkService {

    private static final int TOKEN_VALIDITY_MINUTES = 10;
    private static final int TOKEN_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TelegramLinkTokenRepository tokenRepository;
    private final VerifiedCustomerRepository customerRepository;

    @Value("${telegram.bot-username}")
    private String botUsername;

    /**
     * Creates a one-time Telegram linking URL.
     */
    @Transactional
    public String createLink(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Only previously SMS-verified customers can link Telegram.
        VerifiedCustomer customer = customerRepository
                .findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Verified customer not found"));

        if (!Boolean.TRUE.equals(customer.getVerified())) {
            throw new IllegalArgumentException("Customer phone is not verified");
        }

        // Remove expired tokens.
        tokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());

        // Only one active token per phone.
        tokenRepository.deleteByPhone(phone);

        // Generate a cryptographically secure token.
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        TelegramLinkToken linkToken = new TelegramLinkToken();

        linkToken.setPhone(phone);
        linkToken.setToken(token);
        linkToken.setCreatedAt(LocalDateTime.now());
        linkToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES));

        tokenRepository.save(linkToken);
        String username = botUsername.replace("@", "").trim();
        String link = "https://t.me/" + username + "?start=" + token;
        log.info("Telegram linking token created for customer " + customer.getName());
        return link;
    }

    /**
     * Completes Telegram linking after the customer starts the bot.
     *
     * @return true if linking succeeded, false otherwise.
     */
    @Transactional
    public boolean completeLink(String token, Long telegramChatId) {

        if (token == null || token.isBlank() || telegramChatId == null) {
            return false;
        }

        TelegramLinkToken linkToken = tokenRepository
                .findByToken(token)
                .orElse(null);

        if (linkToken == null) {
            log.warn("Telegram linking token not found");
            return false;
        }

        // Check token expiration.
        if (linkToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(linkToken);
            log.info("Expired Telegram linking token removed");
            return false;
        }

        VerifiedCustomer customer = customerRepository
                .findByPhone(linkToken.getPhone())
                .orElse(null);

        if (customer == null || !Boolean.TRUE.equals(customer.getVerified())) {
            tokenRepository.delete(linkToken);
            log.warn("Verified customer not found for Telegram linking");
            return false;
        }

        // Save Telegram account.
        customer.setTelegramChatId(telegramChatId);
        customer.setTelegramEnabled(true);
        customerRepository.save(customer);

        // Token can only be used once.
        tokenRepository.delete(linkToken);
        log.info("Telegram successfully linked to verified customer");
        return true;
    }
}
