package fr.i360matt.sokeese.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Allows to generate a Sha256 of a character string.
 * @version 1.0.0
 */
public final class Sha256 {
    public static byte[] getSHA (final String input) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHexString (final byte[] hash) {
        final BigInteger number = new BigInteger(1, hash);
        // deepcode ignore missing~append~java.lang.StringBuilder: <checked>
        final StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
            hexString.insert(0, '0');
        return hexString.toString();
    }

    public static String hash (final String input) {
        return toHexString(getSHA(input));
    }
}