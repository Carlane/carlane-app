package com.cherry.alok.myapplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationNumberValidator {

    private Pattern pattern;
    private Matcher matcher;
    //http://stackoverflow.com/questions/6386300/want-a-regex-for-validating-indian-vehicle-number-format
    private static final String REGISTRION_PATTERN = "^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$";
   // private static final String REGISTRION_PATTERN = "^[A-Z]{2}[ -][0-9]{1,2}(?: [A-Z])?(?: [A-Z]*)? [0-9]{4}$";
    private static final String MOBILE_PATTERN = "^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}$";
    public RegistrationNumberValidator() {
        pattern = Pattern.compile(REGISTRION_PATTERN);
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean validate(final String hex) {

        matcher = pattern.matcher(hex);
        return matcher.matches();

    }

    public boolean validateMobile(final String hex)
    {
        pattern = Pattern.compile(MOBILE_PATTERN);
        matcher = pattern.matcher(hex);
        return matcher.matches();
    }
}
