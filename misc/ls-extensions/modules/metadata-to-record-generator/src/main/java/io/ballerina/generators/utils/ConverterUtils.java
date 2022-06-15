package io.ballerina.generators.utils;

import java.util.Optional;
/**
 * Util methods for JSON to custom record converter.
 **/
public class ConverterUtils {
    /**
     * Method for mapping SalesforceMetadataConverter data type to ballerina type.
     *
     * @param type  SalesforceMetadataConverter parameter types
     * @return {@link String} Ballerina type
     */
    public static String convertSalesforceTypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case Constants.SF_INTEGER_TYPE:
                convertedType = "int";
                break;
            case Constants.SF_STRING_TYPE:
            case Constants.SF_DATE_TYPE:
            case Constants.SF_DATETIME_TYPE:
            case Constants.SF_BASE64_TYPE:
            case Constants.SF_ID_TYPE:
            case Constants.SF_REFERENCE_TYPE:
            case Constants.SF_TEXTAREA_TYPE:
            case Constants.SF_PHONE_TYPE:
            case Constants.SF_URL_TYPE:
            case Constants.SF_EMAIL_TYPE:
                convertedType = "string";
                break;
            case Constants.SF_BOOLEAN_TYPE:
                convertedType = "boolean";
                break;
            case Constants.SF_CURRENCY_TYPE:
            case Constants.SF_PERCENT_TYPE:
            case Constants.SF_DOUBLE_TYPE:
                convertedType = "float";
                break;
            default:
                convertedType = "anydata";
        }
        return convertedType;
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier Identifier or method name
     * @return {@link String} Escaped string
     */
    public static String escapeIdentifier(String identifier) {

        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b")
                || Constants.BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

            // TODO: Remove this `if`. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/23045
            if (identifier.equals("error")) {
                identifier = "_error";
            } else {
                identifier = identifier.replaceAll(Constants.ESCAPE_PATTERN, "\\\\$1");
                if (identifier.endsWith("?")) {
                    if (identifier.charAt(identifier.length() - 2) == '\\') {
                        StringBuilder stringBuilder = new StringBuilder(identifier);
                        stringBuilder.deleteCharAt(identifier.length() - 2);
                        identifier = stringBuilder.toString();
                    }
                    if (Constants.BAL_KEYWORDS.stream().anyMatch(Optional.ofNullable(identifier)
                            .filter(sStr -> sStr.length() != 0)
                            .map(sStr -> sStr.substring(0, sStr.length() - 1))
                            .orElse(identifier)::equals)) {
                        identifier = "'" + identifier;
                    } else {
                        return identifier;
                    }
                } else {
                    identifier = "'" + identifier;
                }
            }
        }
        return identifier;
    }
}

