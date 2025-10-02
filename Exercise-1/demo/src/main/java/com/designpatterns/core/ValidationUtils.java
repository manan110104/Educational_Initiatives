package com.designpatterns.core;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Comprehensive validation utilities implementing defensive programming principles.
 * Provides validation at all levels as required by the assignment.
 */
public final class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that an object is not null.
     */
    public static <T> T requireNonNull(T obj, String paramName) throws ApplicationException {
        if (obj == null) {
            throw new ApplicationException(
                String.format("Parameter '%s' cannot be null", paramName),
                "VALIDATION_NULL_PARAMETER",
                false
            );
        }
        return obj;
    }

    /**
     * Validates that a string is not null or empty.
     */
    public static String requireNonEmpty(String str, String paramName) throws ApplicationException {
        requireNonNull(str, paramName);
        if (str.trim().isEmpty()) {
            throw new ApplicationException(
                String.format("Parameter '%s' cannot be empty", paramName),
                "VALIDATION_EMPTY_PARAMETER",
                false
            );
        }
        return str;
    }

    /**
     * Validates that a collection is not null or empty.
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String paramName) throws ApplicationException {
        requireNonNull(collection, paramName);
        if (collection.isEmpty()) {
            throw new ApplicationException(
                String.format("Collection '%s' cannot be empty", paramName),
                "VALIDATION_EMPTY_COLLECTION",
                false
            );
        }
        return collection;
    }

    /**
     * Validates that a number is within a specified range.
     */
    public static <T extends Comparable<T>> T requireInRange(T value, T min, T max, String paramName) throws ApplicationException {
        requireNonNull(value, paramName);
        requireNonNull(min, "min");
        requireNonNull(max, "max");
        
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ApplicationException(
                String.format("Parameter '%s' must be between %s and %s, but was %s", 
                            paramName, min, max, value),
                "VALIDATION_OUT_OF_RANGE",
                false
            );
        }
        return value;
    }

    /**
     * Validates that a string matches a specific pattern.
     */
    public static String requirePattern(String str, Pattern pattern, String paramName, String description) throws ApplicationException {
        requireNonEmpty(str, paramName);
        if (!pattern.matcher(str).matches()) {
            throw new ApplicationException(
                String.format("Parameter '%s' must match %s pattern", paramName, description),
                "VALIDATION_PATTERN_MISMATCH",
                false
            );
        }
        return str;
    }

    /**
     * Validates email format.
     */
    public static String requireValidEmail(String email) throws ApplicationException {
        return requirePattern(email, EMAIL_PATTERN, "email", "valid email");
    }

    /**
     * Validates phone number format.
     */
    public static String requireValidPhone(String phone) throws ApplicationException {
        return requirePattern(phone, PHONE_PATTERN, "phone", "valid phone number");
    }

    /**
     * Validates using a custom predicate.
     */
    public static <T> T requireCondition(T value, Predicate<T> condition, String paramName, String errorMessage) throws ApplicationException {
        requireNonNull(value, paramName);
        if (!condition.test(value)) {
            throw new ApplicationException(
                String.format("Parameter '%s': %s", paramName, errorMessage),
                "VALIDATION_CONDITION_FAILED",
                false
            );
        }
        return value;
    }

    /**
     * Validates that a string has a minimum length.
     */
    public static String requireMinLength(String str, int minLength, String paramName) throws ApplicationException {
        requireNonEmpty(str, paramName);
        if (str.length() < minLength) {
            throw new ApplicationException(
                String.format("Parameter '%s' must be at least %d characters long", paramName, minLength),
                "VALIDATION_MIN_LENGTH",
                false
            );
        }
        return str;
    }

    /**
     * Validates that a string has a maximum length.
     */
    public static String requireMaxLength(String str, int maxLength, String paramName) throws ApplicationException {
        requireNonNull(str, paramName);
        if (str.length() > maxLength) {
            throw new ApplicationException(
                String.format("Parameter '%s' must be at most %d characters long", paramName, maxLength),
                "VALIDATION_MAX_LENGTH",
                false
            );
        }
        return str;
    }
}
