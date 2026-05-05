package com.acculynx.model;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone: digits, spaces, dashes, parentheses, optional +
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s]?[0-9]{3,4}[-\\s]?[0-9]{4,6}$");

    // Zip: 5 digits or 5+4 format (US)
    private static final Pattern ZIP_PATTERN =
            Pattern.compile("^[0-9]{5}(-[0-9]{4})?$");

//    Required field — must not be blank.
    public static String isBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " cannot be empty.";
        }
        return null; // valid
    }


//   Email — must contain '@' and a '.' after the '@'.
    public static String validateEmail(String value) {
        String required = isBlank(value, "Email");
        if (required != null) return required;

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            return "Invalid email — domain must contain '.' (e.g. abc@example.com)";
        }
        return null;
    }


//      Phone — digits only, exactly 10 digits.

    public static String validatePhone(String value) {
        String required = isBlank(value, "Phone");
        if (required != null) return required;

        if (!PHONE_PATTERN.matcher(value).matches()) {
            return "Invalid phone — must be 10 digits (e.g. 5551234567)";
        }
        return null;
    }


//    Zip code — exactly 5 digits.

    public static String validateZip(String value) {
        String required = isBlank(value, "Zip Code");
        if (required != null) return required;

        if (!ZIP_PATTERN.matcher(value).matches()) {
            return "Invalid zip code — must be exactly 5 digits (e.g. 78701)";
        }
        return null;
    }

    public static String validateState(String value) {
        return isBlank(value, "State");
    }
}
