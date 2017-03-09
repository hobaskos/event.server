package io.hobaskos.event.service.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating random Strings.
 */
public final class RandomUtil {

    private static final int DEF_COUNT = 20;

    private RandomUtil() {
    }

    /**
     *
     */
    public static String generateLogin() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a password.
     *
     * @return the generated password
     */
    public static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
    }

    public static String generateRandomEmail() {
        return generateLogin() + "@plod.io";
    }

    /**
     * Generates an activation key.
     *
     * @return the generated activation key
     */
    public static String generateActivationKey() {
        return RandomStringUtils.randomNumeric(DEF_COUNT);
    }

    /**
    * Generates a reset key.
    *
    * @return the generated reset key
    */
    public static String generateResetKey() {
        return RandomStringUtils.randomNumeric(DEF_COUNT);
    }

    /**
     * Generate a random invite code
     * @return
     */
    public static String generateRandomInviteCode() {
        Calendar c = Calendar.getInstance();
        return String.format("%s%s-%d%d%d",
            RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomNumeric(2),
            c.get(Calendar.DAY_OF_WEEK), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR));
    }
}
