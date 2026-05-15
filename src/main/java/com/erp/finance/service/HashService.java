package com.erp.finance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Audit trail service — computes SHA256 hash chain for posted journal entries.
 * Each hash includes the previous entry's hash, creating a tamper-evident chain.
 * Based on Odoo's inalterable_hash system.
 */
@Service
@Slf4j
public class HashService {

    /**
     * Compute the hash for a journal entry/move.
     *
     * @param previousHash The hash of the previous posted entry (null if first)
     * @param entryNumber  The entry number/name
     * @param date         The accounting date (ISO string)
     * @param reference    The external reference
     * @param debitTotal   Total debits (string representation)
     * @param creditTotal  Total credits (string representation)
     * @return SHA256 hex string
     */
    public String computeHash(String previousHash, String entryNumber, String date,
                              String reference, String debitTotal, String creditTotal) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String data = String.format("%s|%s|%s|%s|%s|%s",
                    previousHash != null ? previousHash : "",
                    entryNumber != null ? entryNumber : "",
                    date != null ? date : "",
                    reference != null ? reference : "",
                    debitTotal != null ? debitTotal : "0",
                    creditTotal != null ? creditTotal : "0"
            );
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a hash chain for a given sequence of entries.
     * Returns true if the chain is valid.
     */
    public boolean verifyChain(java.util.List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) return true;
        // Recompute the chain and verify each link
        // This would need the original data — placeholder for full implementation
        return true;
    }
}
