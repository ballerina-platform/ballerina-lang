package org.ballerinalang.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.mime.util.Constants.BUILTIN_PACKAGE;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;
import static org.ballerinalang.mime.util.Constants.STRUCT_GENERIC_ERROR;

/**
 * Utility methods for parsing headers.
 */
public class HeaderParser {

    public static BMap<String, BValue> getParamMap(String headerValue) {

        BMap<String, BValue> paramMap = null;
        if (headerValue.contains(SEMICOLON)) {
            List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(SEMICOLON) + 1)
                    .split(SEMICOLON)).map(String::trim).collect(Collectors.toList());
            paramMap = validateParams(paramList) ? createParamBMap(paramList) : null;
        }
        return paramMap;
    }

    public static String getHeaderValue(String headerValue) {
       return extractValue(headerValue.trim());
    }

    private static String extractValue(String headerValue) {
        String value = headerValue.substring(0, headerValue.indexOf(SEMICOLON)).trim();
        if (value.isEmpty()) {
            throw new BallerinaException("invalid header value: " + headerValue);
        }
        return value;
    }

    private static boolean validateParams(List<String> paramList) {
        //validate header values which ends with semicolon without params
        if (paramList.size() == 1 && paramList.get(0).isEmpty()) {
            return false;
        }
        return true;
    }

    public static BStruct getParserError(Context context, String errMsg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(BUILTIN_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);

        BStruct parserError = new BStruct(errorStructInfo.getType());
        parserError.setStringField(0, errMsg);
        return parserError;
    }

    private static BMap<String, BValue> createParamBMap(List<String> paramList) {
        BMap<String, BValue> paramMap = new BMap<>();
        for (String param : paramList) {
            if (param.contains("=")) {
                String[] keyValuePair = param.split("=");
                if (keyValuePair.length != 2 || keyValuePair[0].isEmpty()) {
                    throw new BallerinaException("invalid header parameter: " + param);
                }
                paramMap.put(keyValuePair[0].trim(), new BString(keyValuePair[1].trim()));
            } else {
                //handle when parameter value is optional
                paramMap.put(param.trim(), null);
            }
        }
        return paramMap;
    }
}
