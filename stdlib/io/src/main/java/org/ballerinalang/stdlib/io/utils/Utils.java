/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.io.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

/**
 * A util class for handling common functions across native implementation.
 *
 * @since 0.95.4
 */
public class Utils {

    public static final String PACKAGE_TIME = "ballerina/time";
    public static final String STRUCT_TYPE_TIME = "Time";
    public static final String STRUCT_TYPE_TIMEZONE = "Timezone";
    public static final int READABLE_BUFFER_SIZE = 8192; //8KB
    public static final String PROTOCOL_PACKAGE_UTIL = "ballerina/util";
    public static final String PROTOCOL_PACKAGE_MIME = "ballerina/mime";
    public static final String BASE64_ENCODE_ERROR = "Base64EncodeError";
    public static final String BASE64_DECODE_ERROR = "Base64DecodeError";
    private static final String STRUCT_TYPE = "ReadableByteChannel";

    public static BMap<String, BValue> createTimeZone(StructureTypeInfo timezoneStructInfo, String zoneIdValue) {
        String zoneIdName;
        try {
            ZoneId zoneId = ZoneId.of(zoneIdValue);
            zoneIdName = zoneId.toString();
            //Get offset in seconds
            TimeZone tz = TimeZone.getTimeZone(zoneId);
            int offsetInMills = tz.getOffset(new Date().getTime());
            int offset = offsetInMills / 1000;
            return BLangVMStructs.createBStruct(timezoneStructInfo, zoneIdName, offset);
        } catch (ZoneRulesException e) {
            throw new BallerinaException("invalid timezone id: " + zoneIdValue);
        }
    }

    public static BMap<String, BValue> createTimeStruct(StructureTypeInfo timezoneStructInfo,
                                           StructureTypeInfo timeStructInfo, long millis, String zoneIdName) {
        BMap<String, BValue> timezone = Utils.createTimeZone(timezoneStructInfo, zoneIdName);
        return BLangVMStructs.createBStruct(timeStructInfo, millis, timezone);
    }

    public static StructureTypeInfo getTimeZoneStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_TIME);
        if (timePackageInfo == null) {
            return null;
        }
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIMEZONE);
    }

    public static StructureTypeInfo getTimeStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_TIME);
        if (timePackageInfo == null) {
            return null;
        }
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIME);
    }

    public static BMap<String, BValue> createConversionError(Context context, String msg) {
        return BLangVMErrors.createError(context, msg);
    }

    private static BMap<String, BValue> createBase64Error(Context context, String msg, boolean isMimeSpecific,
                                                          boolean isEncoder) {
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
     * Given an input stream, get a byte array.
     *
     * @param input Represent an input stream
     * @return A byte array
     * @throws IOException In case an error occurs while reading input stream
     */
    private static byte[] getByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[READABLE_BUFFER_SIZE];
            for (int len; (len = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }

    /**
     * Encode a given BValue using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param input          Represent a BValue which can be of type blob, string or byte channel
     * @param charset        Represent the charset value to be used with string
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    @SuppressWarnings("unchecked")
    public static void encode(Context context, BValue input, String charset, boolean isMimeSpecific) {
        switch (input.getType().getTag()) {
            case TypeTags.ARRAY_TAG:
                encodeBlob(context, ((BByteArray) input).getBytes(), isMimeSpecific);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                BMap<String, BValue> byteChannel = (BMap<String, BValue>) input;
                if (STRUCT_TYPE.equals(byteChannel.getType().getName())) {
                    encodeByteChannel(context, byteChannel, isMimeSpecific);
                }
                break;
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
    @SuppressWarnings("unchecked")
    public static void decode(Context context, BValue encodedInput, String charset, boolean isMimeSpecific) {
        switch (encodedInput.getType().getTag()) {
            case TypeTags.ARRAY_TAG:
                decodeBlob(context, ((BByteArray) encodedInput).getBytes(), isMimeSpecific);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                decodeByteChannel(context, (BMap<String, BValue>) encodedInput, isMimeSpecific);
                break;
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
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
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
            context.setReturnValues(Utils.createBase64Error(context, e.getMessage(), isMimeSpecific, false));
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
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
        }
    }

    /**
     * Encode a given byte channel using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param byteChannel    Represent the byte channel that needs to be encoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    public static void encodeByteChannel(Context context, BMap<String, BValue> byteChannel, boolean isMimeSpecific) {
        Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        BMap<String, BValue> byteChannelStruct;
        try {
            byte[] encodedByteArray;
            if (isMimeSpecific) {
                encodedByteArray = Base64.getMimeEncoder().encode(Utils.getByteArray(
                        channel.getInputStream()));
            } else {
                encodedByteArray = Base64.getEncoder().encode(Utils.getByteArray(
                        channel.getInputStream()));
            }
            InputStream encodedStream = new ByteArrayInputStream(encodedByteArray);
            Base64ByteChannel decodedByteChannel = new Base64ByteChannel(encodedStream);
            byteChannelStruct = BLangConnectorSPIUtil.createBStruct(context,
                    IOConstants.IO_PACKAGE, STRUCT_TYPE);
            byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                    new Base64Wrapper(decodedByteChannel));
            context.setReturnValues(byteChannelStruct);
        } catch (IOException e) {
            context.setReturnValues(Utils.createBase64Error(context, e.getMessage(), isMimeSpecific, true));
        }
    }

    /**
     * Decode a given byte channel using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param byteChannel    Represent the byte channel that needs to be decoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    public static void decodeByteChannel(Context context, BMap<String, BValue> byteChannel, boolean isMimeSpecific) {
        if (STRUCT_TYPE.equals(byteChannel.getType().getName())) {
            Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            BMap<String, BValue> byteChannelStruct;
            byte[] decodedByteArray;
            try {
                if (isMimeSpecific) {
                    decodedByteArray = Base64.getMimeDecoder().decode(Utils.getByteArray(
                            channel.getInputStream()));
                } else {
                    decodedByteArray = Base64.getDecoder().decode(Utils.getByteArray(
                            channel.getInputStream()));
                }
                InputStream decodedStream = new ByteArrayInputStream(decodedByteArray);
                Base64ByteChannel decodedByteChannel = new Base64ByteChannel(decodedStream);
                byteChannelStruct = BLangConnectorSPIUtil.createBStruct(context,
                        IOConstants.IO_PACKAGE, STRUCT_TYPE);
                byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                        new Base64Wrapper(decodedByteChannel));
                context.setReturnValues(byteChannelStruct);
            } catch (IOException e) {
                context.setReturnValues(Utils.createBase64Error(context, e.getMessage(), isMimeSpecific,
                        false));
            }
        }
    }

    /**
     * Encode a given blob using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param bytes          Represent the blob that needs to be encoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    public static void encodeBlob(Context context, byte[] bytes, boolean isMimeSpecific) {
        byte[] encodedContent;
        if (isMimeSpecific) {
            encodedContent = Base64.getMimeEncoder().encode(bytes);
        } else {
            encodedContent = Base64.getEncoder().encode(bytes);
        }
        context.setReturnValues(new BByteArray(encodedContent));
    }


    /**
     * Decode a given blob using Base64 encoding scheme.
     *
     * @param context        Represent a ballerina context
     * @param encodedContent Represent the blob that needs to be decoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     */
    public static void decodeBlob(Context context, byte[] encodedContent, boolean isMimeSpecific) {
        byte[] decodedContent;
        if (isMimeSpecific) {
            decodedContent = Base64.getMimeDecoder().decode(encodedContent);
        } else {
            decodedContent = Base64.getDecoder().decode(encodedContent);
        }
        context.setReturnValues(new BByteArray(decodedContent));
    }
}
