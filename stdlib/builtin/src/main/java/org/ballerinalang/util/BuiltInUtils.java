/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @since 0.94.1
 */
public class BuiltInUtils {

    private static final String PROTOCOL_PACKAGE_UTIL = "ballerina.util";
    private static final String PROTOCOL_PACKAGE_MIME = "ballerina.mime";
    private static final String BASE64_ENCODE_ERROR = "Base64EncodeError";
    private static final String BASE64_DECODE_ERROR = "Base64DecodeError";
    private static final int READABLE_BUFFER_SIZE = 8192; //8KB

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link BValue} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static BValue getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return BTypes.typeString.getZeroValue();
        }
        return new BString(value);
    }

    /**
     * Get builtin conversion error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @return conversion error
     */
    public static BError createConversionError(Context context, String errMsg) {
        return createError(context, errMsg, "{ballerina/builtin}ConversionError");
    }

    /**
     * Get builtin string error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @return conversion error
     */
    public static BError createStringError(Context context, String errMsg) {
        return createError(context, errMsg, "{ballerina/builtin}StringError");
    }

    /**
     * Get builtin conversion error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @param reason The reason of the error
     * @return conversion error
     */
    public static BError createError(Context context, String errMsg, String reason) {
        BMap<String, BValue> errorMap = new BMap<>();
        errorMap.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, reason, errorMap);
    }

    private static BMap<String, BValue> createBase64Error(Context context, String msg,
                                                          boolean isMimeSpecific, boolean isEncoder) {
        PackageInfo filePkg;
        if (isMimeSpecific) {
            filePkg = context.getProgramFile().getPackageInfo(PROTOCOL_PACKAGE_MIME);
        } else {
            filePkg = context.getProgramFile().getPackageInfo(PROTOCOL_PACKAGE_UTIL);
        }
        StructureTypeInfo entityErrInfo = filePkg.getStructInfo(isEncoder ? BASE64_ENCODE_ERROR : BASE64_DECODE_ERROR);
        return BLangVMStructs.createBStruct(entityErrInfo, msg);
    }

    /**
     * Encode a given BValue using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param input          Represent a BValue which can be of type blob, string or byte channel
     * @param charset        Represent the charset value to be used with string
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    public static void encode(Context context, BValue input, String charset, boolean isMimeSpecific) {
        switch (input.getType().getTag()) {
            case TypeTags.STRING_TAG:
                encodeString(context, input.stringValue(), charset, isMimeSpecific);
                break;
            default:
                break;
        }
    }

    /**
     * Decode a given BValue using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param encodedInput   Represent an encoded BValue which can be of type blob, string or byte channel
     * @param charset        Represent the charset value to be used with string
     * @param isMimeSpecific A boolean indicating whether the decoder should be mime specific or not
     */
    public static void decode(Context context, BValue encodedInput, String charset, boolean isMimeSpecific) {
        switch (encodedInput.getType().getTag()) {
            case TypeTags.STRING_TAG:
                decodeString(context, encodedInput, charset, isMimeSpecific);
                break;
            default:
                break;
        }
    }

    /**
     * Encode a given string using Base64 encoding scheme.
     *
     * @param context           Represent a ballerina context
     * @param stringToBeEncoded Represent the string that needs to be encoded
     * @param charset           Represent the charset value to be used with string
     * @param isMimeSpecific    A boolean indicating whether the encoder should be mime specific or not
     */
    public static void encodeString(Context context, String stringToBeEncoded, String charset, boolean isMimeSpecific) {
        try {
            byte[] encodedValue;
            if (isMimeSpecific) {
                encodedValue = Base64.getMimeEncoder().encode(stringToBeEncoded.getBytes(charset));
            } else {
                encodedValue = Base64.getEncoder().encode(stringToBeEncoded.getBytes(charset));
            }
            context.setReturnValues(new BString(new String(encodedValue, StandardCharsets.ISO_8859_1)));
        } catch (UnsupportedEncodingException e) {
            context.setReturnValues(createStringError(context, e.getMessage()));
        }
    }

    /**
     * Decode a given encoded string using Base64 encoding scheme.
     *
     * @param context           Represent a ballerina context
     * @param stringToBeDecoded Represent the string that needs to be decoded
     * @param charset           Represent the charset value to be used with string
     * @param isMimeSpecific    A boolean indicating whether the decoder should be mime specific or not
     */
    private static void decodeString(Context context, BValue stringToBeDecoded, String charset,
                                     boolean isMimeSpecific) {
        try {
            byte[] decodedValue;
            if (isMimeSpecific) {
                decodedValue = Base64.getMimeDecoder().decode(stringToBeDecoded.stringValue()
                                                                      .getBytes(StandardCharsets.ISO_8859_1));
            } else {
                decodedValue = Base64.getDecoder().decode(stringToBeDecoded.stringValue()
                                                                  .getBytes(StandardCharsets.ISO_8859_1));
            }
            context.setReturnValues(new BString(new String(decodedValue, charset)));
        } catch (UnsupportedEncodingException e) {
            context.setReturnValues(createBase64Error(context, e.getMessage(), isMimeSpecific, false));
        }
    }

    /**
     * Decode a given encoded string using Base64 encoding scheme.
     *
     * @param context           Represent a ballerina context
     * @param stringToBeDecoded Represent the string that needs to be decoded
     * @param charset           Represent the charset value to be used with string
     * @param isMimeSpecific    A boolean indicating whether the decoder should be mime specific or not
     */
    public static void decodeString(Context context, String stringToBeDecoded, String charset, boolean isMimeSpecific) {
        try {
            byte[] decodedValue;
            if (isMimeSpecific) {
                decodedValue = Base64.getMimeDecoder().decode(stringToBeDecoded.getBytes(StandardCharsets.ISO_8859_1));
            } else {
                decodedValue = Base64.getDecoder().decode(stringToBeDecoded.getBytes(StandardCharsets.ISO_8859_1));
            }
            context.setReturnValues(new BString(new String(decodedValue, charset)));
        } catch (UnsupportedEncodingException e) {
            context.setReturnValues(BuiltInUtils.createStringError(context, e.getMessage()));
        }
    }

    private BuiltInUtils() {
    }
}
