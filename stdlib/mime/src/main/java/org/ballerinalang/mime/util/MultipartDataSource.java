/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.util;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.api.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.BOUNDARY;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_ID;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_ID_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.LEFT_ANGLE_BRACKET;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RIGHT_ANGLE_BRACKET;

/**
 * Act as multipart encoder.
 *
 * @since 0.963.0
 */
public class MultipartDataSource implements RefValue {
    private static final Logger log = LoggerFactory.getLogger(MultipartDataSource.class);

    private ObjectValue parentEntity;
    private String boundaryString;
    private OutputStream outputStream;
    private static final String CRLF_POST_DASH = "\r\n--";
    private static final String CRLF_PRE_DASH = "--\r\n";
    private static final String CRLF = "\r\n";
    private static final char COLON = ':';
    private static final char SPACE = ' ';

    public MultipartDataSource(ObjectValue entityStruct, String boundaryString) {
        this.parentEntity = entityStruct;
        this.boundaryString = boundaryString;
    }

    @Override
    public void serialize(OutputStream outputStream) {
        this.outputStream = outputStream;
        serializeBodyPart(outputStream, boundaryString, parentEntity);
    }

    /**
     * Serialize body parts including nested parts within them.
     *
     * @param outputStream         Represent the outputstream that the body parts will be written to
     * @param parentBoundaryString Represent the parent boundary string
     * @param parentBodyPart       Represent parent body part
     */
    private void serializeBodyPart(OutputStream outputStream, String parentBoundaryString,
                                   ObjectValue parentBodyPart) {
        ArrayValue childParts = parentBodyPart.getNativeData(BODY_PARTS) != null ?
                (ArrayValue) parentBodyPart.getNativeData(BODY_PARTS) : null;
        try (final Writer outputStreamWriter = new OutputStreamWriter(outputStream, Charset.defaultCharset());
             final Writer writer = new BufferedWriter(outputStreamWriter)) {
            if (childParts == null) {
                return;
            }
            for (int i = 0; i < childParts.size(); i++) {
                ObjectValue childPart = (ObjectValue) childParts.getRefValue(i);
                // Write leading boundary string
                writer.write(CRLF_POST_DASH);
                writer.write(parentBoundaryString);
                writer.write(CRLF);
                checkForNestedParts(writer, childPart);
                writeBodyContent(outputStream, childPart);
            }
            writeFinalBoundaryString(writer, parentBoundaryString);
        } catch (IOException e) {
            log.error("Error occurred while writing body parts to outputstream", e.getMessage());
        }
    }

    /**
     * If child part has nested parts, get a new boundary string and set it to Content-Type. After that write the
     * child part headers to outputstream and serialize its nested parts if it has any.
     *
     * @param writer    Represent the outputstream writer
     * @param childPart Represent a child part
     * @throws IOException When an error occurs while writing child part headers
     */
    @SuppressWarnings("unchecked")
    private void checkForNestedParts(Writer writer, ObjectValue childPart) throws IOException {
        String childBoundaryString = null;
        if (MimeUtil.isNestedPartsAvailable(childPart)) {
            childBoundaryString = MimeUtil.getNewMultipartDelimiter();
            ObjectValue mediaType = (ObjectValue) childPart.get(MEDIA_TYPE_FIELD);
            MapValue paramMap;
            if (mediaType.get(PARAMETER_MAP_FIELD) != null) {
                paramMap = (MapValue) mediaType.get(PARAMETER_MAP_FIELD);
            } else {
                paramMap = new MapValueImpl<>(new org.ballerinalang.jvm.types.BMapType(BTypes.typeString));
            }

            paramMap.put(BOUNDARY, childBoundaryString);
            mediaType.set(PARAMETER_MAP_FIELD, paramMap);
        }
        writeBodyPartHeaders(writer, childPart);
        //Serialize nested parts
        if (childBoundaryString != null) {
            ArrayValue nestedParts = (ArrayValue) childPart.getNativeData(BODY_PARTS);
            if (nestedParts != null && nestedParts.size() > 0) {
                serializeBodyPart(this.outputStream, childBoundaryString, childPart);
            }
        }
    }

    /**
     * Write body part headers to output stream.
     *
     * @param writer   Represent the outputstream writer
     * @param bodyPart Represent ballerina body part
     * @throws IOException When an error occurs while writing body part headers
     */
    private void writeBodyPartHeaders(Writer writer, ObjectValue bodyPart) throws IOException {
        HttpHeaders httpHeaders;
        if (bodyPart.getNativeData(ENTITY_HEADERS) != null) {
            httpHeaders = (HttpHeaders) bodyPart.getNativeData(ENTITY_HEADERS);
        } else {
            httpHeaders = new DefaultHttpHeaders();
            bodyPart.addNativeData(ENTITY_HEADERS, httpHeaders);
        }
        String contentType = MimeUtil.getContentTypeWithParameters(bodyPart);
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (!contentDisposition.isEmpty()) {
            httpHeaders.set(HttpHeaderNames.CONTENT_DISPOSITION.toString(), contentDisposition);
        }
        
        Object contentId = bodyPart.get(CONTENT_ID_FIELD);
        if (contentId != null && !contentId.toString().isEmpty()) {
            // Content-ID should be enclosed with angle brackets
            httpHeaders.set(CONTENT_ID, LEFT_ANGLE_BRACKET + contentId.toString() + RIGHT_ANGLE_BRACKET);
        }
        Iterator<Map.Entry<String, String>> iterator = httpHeaders.iteratorAsString();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            writer.write(entry.getKey());
            writer.write(COLON);
            writer.write(SPACE);
            writer.write(entry.getValue());
            writer.write(CRLF);
        }
        // Mark the end of the headers for this body part
        writer.write(CRLF);
        writer.flush();
    }

    /**
     * Write the final boundary string.
     *
     * @param writer         Represent an outputstream writer
     * @param boundaryString Represent the boundary as a string
     * @throws IOException When an error occurs while writing final boundary string
     */
    private void writeFinalBoundaryString(Writer writer, String boundaryString) throws IOException {
        writer.write(CRLF_POST_DASH);
        writer.write(boundaryString);
        writer.write(CRLF_PRE_DASH);
        writer.flush();
    }

    /**
     * Write body part content to outputstream.
     *
     * @param outputStream Represent an outputstream
     * @param bodyPart     Represent a ballerina body part
     * @throws IOException When an error occurs while writing body content
     */
    private void writeBodyContent(OutputStream outputStream, ObjectValue bodyPart) throws IOException {
        Object messageDataSource = EntityBodyHandler.getMessageDataSource(bodyPart);
        if (messageDataSource != null) {
            //TODO Recheck following logic
            if (messageDataSource instanceof String || messageDataSource instanceof Long ||
                    messageDataSource instanceof Double || messageDataSource instanceof Integer ||
                    messageDataSource instanceof Boolean) {
                outputStream.write(messageDataSource.toString().getBytes(Charset.defaultCharset()));
            } else {
                ((RefValue) messageDataSource).serialize(outputStream);
            }
        } else {
            EntityBodyHandler.writeByteChannelToOutputStream(bodyPart, outputStream);
        }
    }

    public String stringValue() {
        return null;
    }

    @Override
    public BString bStringValue() {
        return null;
    }

    @Override
    public org.ballerinalang.jvm.types.BType getType() {
        return null;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {

        throw new UnsupportedOperationException();
    }
}
