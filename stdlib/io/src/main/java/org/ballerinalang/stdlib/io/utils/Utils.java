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

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.stdlib.io.channels.base.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * A util class for handling common functions across native implementation.
 *
 * @since 0.95.4
 */
public class Utils {

    private static final int READABLE_BUFFER_SIZE = 8192; //8KB
    private static final Module PACKAGE_ID_MIME = new Module(BALLERINA_BUILTIN_PKG_PREFIX, "mime", "1.0.0");
    private static final String STRUCT_TYPE = "ReadableByteChannel";
    private static final String ENCODING_ERROR = "EncodeError";
    private static final String DECODING_ERROR = "DecodeError";


    private static BError createBase64Error(String errorType, String msg, boolean isMimeSpecific) {
        if (isMimeSpecific) {
            return ErrorCreator.createDistinctError(errorType, PACKAGE_ID_MIME, StringUtils.fromString(msg));
        }
        return IOUtils.createError(IOConstants.ErrorCode.GenericError, msg);
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
     * @param input          Represent a BValue which can be of type blob, string or byte channel
     * @param charset        Represent the charset value to be used with string
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     * @return encoded value
     */
    @SuppressWarnings("unchecked")
    public static Object encode(Object input, String charset, boolean isMimeSpecific) {
        switch (TypeChecker.getType(input).getTag()) {
            case TypeTags.ARRAY_TAG:
                return encodeBlob(((BArray) input).getBytes(), isMimeSpecific);
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                //TODO : recheck following casing
                BObject byteChannel = (BObject) input;
                if (STRUCT_TYPE.equals(byteChannel.getType().getName())) {
                    return encodeByteChannel(byteChannel, isMimeSpecific);
                }
                return Utils.createBase64Error(ENCODING_ERROR, "incompatible object", isMimeSpecific);
            case TypeTags.STRING_TAG:
                return encodeString(input.toString(), charset, isMimeSpecific);
            default:
                return Utils.createBase64Error(ENCODING_ERROR, "incompatible input", isMimeSpecific);
        }
    }

    /**
     * Decode a given BValue using Base64 encoding scheme.
     *
     * @param encodedInput   Represent an encoded BValue which can be of type blob, string or byte channel
     * @param charset        Represent the charset value to be used with string
     * @param isMimeSpecific A boolean indicating whether the decoder should be mime specific or not
     * @return decoded value
     */
    public static Object decode(Object encodedInput, String charset, boolean isMimeSpecific) {
        switch (TypeChecker.getType(encodedInput).getTag()) {
            case TypeTags.ARRAY_TAG:
                return decodeBlob(((BArray) encodedInput).getBytes(), isMimeSpecific);
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return decodeByteChannel((BObject) encodedInput, isMimeSpecific);
            case TypeTags.STRING_TAG:
                return decodeString(encodedInput, charset, isMimeSpecific);
            default:
                return Utils.createBase64Error(DECODING_ERROR, "incompatible input", isMimeSpecific);
        }
    }

    /**
     * Encode a given string using Base64 encoding scheme.
     *
     * @param stringToBeEncoded Represent the string that needs to be encoded
     * @param charset           Represent the charset value to be used with string
     * @param isMimeSpecific    A boolean indicating whether the encoder should be mime specific or not
     * @return encoded string or an error
     */
    public static Object encodeString(String stringToBeEncoded, String charset, boolean isMimeSpecific) {
        try {
            byte[] encodedValue;
            if (isMimeSpecific) {
                encodedValue = Base64.getMimeEncoder().encode(stringToBeEncoded.getBytes(charset));
            } else {
                encodedValue = Base64.getEncoder().encode(stringToBeEncoded.getBytes(charset));
            }
            return new String(encodedValue, StandardCharsets.ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            return Utils.createBase64Error(DECODING_ERROR, e.getMessage(), isMimeSpecific);
        }
    }

    /**
     * Decode a given encoded string using Base64 encoding scheme.
     *
     * @param stringToBeDecoded Represent the string that needs to be decoded
     * @param charset           Represent the charset value to be used with string
     * @param isMimeSpecific    A boolean indicating whether the decoder should be mime specific or not
     */
    private static Object decodeString(Object stringToBeDecoded, String charset, boolean isMimeSpecific) {
        try {
            byte[] decodedValue;
            if (isMimeSpecific) {
                decodedValue = Base64.getMimeDecoder().decode(stringToBeDecoded.toString()
                                                                      .getBytes(StandardCharsets.ISO_8859_1));
            } else {
                decodedValue = Base64.getDecoder().decode(stringToBeDecoded.toString()
                                                                  .getBytes(StandardCharsets.ISO_8859_1));
            }
           return new String(decodedValue, charset);
        } catch (UnsupportedEncodingException e) {
            return Utils.createBase64Error(DECODING_ERROR, e.getMessage(), isMimeSpecific);
        }
    }

    /**
     * Encode a given byte channel using Base64 encoding scheme.
     *
     * @param byteChannel    Represent the byte channel that needs to be encoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     * @return encoded ReadableByteChannel or an error
     */
    public static Object encodeByteChannel(BObject byteChannel, boolean isMimeSpecific) {
        Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        BObject byteChannelObj;
        try {
            byte[] encodedByteArray;
            if (isMimeSpecific) {
                encodedByteArray = Base64.getMimeEncoder().encode(Utils.getByteArray(channel.getInputStream()));
            } else {
                encodedByteArray = Base64.getEncoder().encode(Utils.getByteArray(channel.getInputStream()));
            }
            InputStream encodedStream = new ByteArrayInputStream(encodedByteArray);
            Base64ByteChannel decodedByteChannel = new Base64ByteChannel(encodedStream);
            byteChannelObj = ValueCreator.createObjectValue(IOConstants.IO_PACKAGE_ID, STRUCT_TYPE);
            byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new Base64Wrapper(decodedByteChannel));
            return byteChannelObj;
        } catch (IOException e) {
            return Utils.createBase64Error(ENCODING_ERROR, e.getMessage(), isMimeSpecific);
        }
    }

    /**
     * Decode a given byte channel using Base64 encoding scheme.
     *
     * @param byteChannel    Represent the byte channel that needs to be decoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     * @return decoded ReadableByteChannel or an error
     */
    public static Object decodeByteChannel(BObject byteChannel, boolean isMimeSpecific) {
        Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        BObject byteChannelObj;
        byte[] decodedByteArray;
        try {
            if (isMimeSpecific) {
                decodedByteArray = Base64.getMimeDecoder().decode(Utils.getByteArray(channel.getInputStream()));
            } else {
                decodedByteArray = Base64.getDecoder().decode(Utils.getByteArray(channel.getInputStream()));
            }
            InputStream decodedStream = new ByteArrayInputStream(decodedByteArray);
            Base64ByteChannel decodedByteChannel = new Base64ByteChannel(decodedStream);
            byteChannelObj = ValueCreator.createObjectValue(IOConstants.IO_PACKAGE_ID, STRUCT_TYPE);
            byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new Base64Wrapper(decodedByteChannel));
            return byteChannelObj;
        } catch (IOException e) {
            return Utils.createBase64Error(DECODING_ERROR, e.getMessage(), isMimeSpecific);
        }
    }

    /**
     * Encode a given blob using Base64 encoding scheme.
     *
     * @param bytes          Represent the blob that needs to be encoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     * @return encoded blob
     */
    public static BArray encodeBlob(byte[] bytes, boolean isMimeSpecific) {
        byte[] encodedContent;
        if (isMimeSpecific) {
            encodedContent = Base64.getMimeEncoder().encode(bytes);
        } else {
            encodedContent = Base64.getEncoder().encode(bytes);
        }
        return ValueCreator.createArrayValue(encodedContent);
    }

    /**
     * Decode a given blob using Base64 encoding scheme.
     *
     * @param encodedContent Represent the blob that needs to be decoded
     * @param isMimeSpecific A boolean indicating whether the encoder should be mime specific or not
     * @return decoded blob
     */
    public static BArray decodeBlob(byte[] encodedContent, boolean isMimeSpecific) {
        byte[] decodedContent;
        if (isMimeSpecific) {
            decodedContent = Base64.getMimeDecoder().decode(encodedContent);
        } else {
            decodedContent = Base64.getDecoder().decode(encodedContent);
        }
        return ValueCreator.createArrayValue(decodedContent);
    }
}
