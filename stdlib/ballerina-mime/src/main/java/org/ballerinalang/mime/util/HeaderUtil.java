package org.ballerinalang.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.jvnet.mimepull.Header;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.mime.util.Constants.BUILTIN_PACKAGE;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_PARA_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.FILENAME_INDEX;
import static org.ballerinalang.mime.util.Constants.FIRST_ELEMENT;
import static org.ballerinalang.mime.util.Constants.NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.STRUCT_GENERIC_ERROR;

/**
 * Utility methods for parsing headers.
 */
public class HeaderUtil {

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

    public static boolean isHeaderExist(List<String> headers) {
        return headers != null && headers.get(FIRST_ELEMENT) != null && !headers.get(FIRST_ELEMENT).isEmpty();
    }

    /**
     * Set body part headers.
     *
     * @param bodyPartHeaders Represent decoded mime part headers
     * @param headerMap       Represent ballerina header map
     * @return a populated ballerina map with body part headers
     */
    public static BMap<String, BValue> setBodyPartHeaders(List<? extends Header> bodyPartHeaders,
                                                          BMap<String, BValue> headerMap) {
        for (final Header header : bodyPartHeaders) {
            if (headerMap.keySet().contains(header.getName())) {
                BStringArray valueArray = (BStringArray) headerMap.get(header.getName());
                valueArray.add(valueArray.size(), header.getValue());
            } else {
                BStringArray valueArray = new BStringArray(new String[]{header.getValue()});
                headerMap.put(header.getName(), valueArray);
            }
        }
        return headerMap;
    }

    /**
     * Populate ContentDisposition struct and set it to body part.
     *
     * @param contentDisposition       Represent the ContentDisposition struct that needs to be filled with values
     * @param bodyPart                 Represent a body part
     * @param contentDispositionHeader Represent Content-Disposition header value with parameters
     */
    public static void setContentDisposition(BStruct contentDisposition, BStruct bodyPart,
                                             String contentDispositionHeader) {
        contentDisposition.setStringField(DISPOSITION_INDEX, HeaderUtil.getHeaderValue(contentDispositionHeader));
        BMap<String, BValue> paramMap = HeaderUtil.getParamMap(contentDispositionHeader);
        if (paramMap != null) {
            Set<String> keys = paramMap.keySet();
            for (String key : keys) {
                BString paramValue = (BString) paramMap.get(key);
                switch (key) {
                    case CONTENT_DISPOSITION_FILE_NAME:
                        contentDisposition.setStringField(FILENAME_INDEX, paramValue.toString());
                        break;
                    case CONTENT_DISPOSITION_NAME:
                        contentDisposition.setStringField(NAME_INDEX, paramValue.toString());
                        break;
                }
            }
        }
        contentDisposition.setRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX, paramMap);
        bodyPart.setRefField(CONTENT_DISPOSITION_INDEX, contentDisposition);
    }

    /**
     * Populate given 'Entity' with it's body size.
     *
     * @param entityStruct Represent 'Entity'
     * @param length       Size of the entity body
     */
    public static void setContentLength(BStruct entityStruct, int length) {
        entityStruct.setIntField(SIZE_INDEX, length);
    }

    /**
     * Get the header value for a given header name from a body part.
     *
     * @param bodyPart   Represent a ballerina body part.
     * @param headerName Represent an http header name
     * @return a header value for the given header name
     */
    public static String getHeaderValue(BStruct bodyPart, String headerName) {
        BMap<String, BValue> headerMap = bodyPart.getRefField(ENTITY_HEADERS_INDEX) != null ?
                (BMap<String, BValue>) bodyPart.getRefField(ENTITY_HEADERS_INDEX) : null;
        if (headerMap != null) {
            BStringArray headerValue = (BStringArray) headerMap.get(headerName);
            return headerValue.get(0);
        }
        return null;
    }
}
