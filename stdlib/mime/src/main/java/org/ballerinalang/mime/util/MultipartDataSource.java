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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
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
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_ID_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;

/**
 * Act as multipart encoder.
 *
 * @since 0.963.0
 */
public class MultipartDataSource implements BRefValue {
    private static final Logger log = LoggerFactory.getLogger(MultipartDataSource.class);

    private BObject parentEntity;
    private String boundaryString;
    private OutputStream outputStream;
    private static final String DASH_BOUNDARY = "--";
    private static final String CRLF_POST_DASH = "\r\n--";
    private static final String CRLF_PRE_DASH = "--\r\n";
    private static final String CRLF = "\r\n";
    private static final char COLON = ':';
    private static final char SPACE = ' ';

    public MultipartDataSource(BObject entityStruct, String boundaryString) {
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
                                   BObject parentBodyPart) {
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.defaultCharset()));
        BArray childParts = parentBodyPart.getNativeData(BODY_PARTS) != null ?
                (BArray) parentBodyPart.getNativeData(BODY_PARTS) : null;
        try {
            if (childParts == null) {
                return;
            }
            boolean firstPart = true;
            for (int i = 0; i < childParts.size(); i++) {
                BObject childPart = (BObject) childParts.getRefValue(i);
                // Write leading boundary string
                if (firstPart) {
                    firstPart = false;
                    writer.write(DASH_BOUNDARY);
                } else {
                    writer.write(CRLF_POST_DASH);
                }
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
    private void checkForNestedParts(Writer writer, BObject childPart) throws IOException {
        String childBoundaryString = null;
        if (MimeUtil.isNestedPartsAvailable(childPart)) {
            childBoundaryString = MimeUtil.getNewMultipartDelimiter();
            BObject mediaType = (BObject) childPart.get(MEDIA_TYPE_FIELD);
            BMap<BString, Object> paramMap;
            if (mediaType.get(PARAMETER_MAP_FIELD) != null) {
                paramMap = (BMap<BString, Object>) mediaType.get(PARAMETER_MAP_FIELD);
            } else {
                paramMap = ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_STRING));
            }

            paramMap.put(StringUtils.fromString(BOUNDARY), StringUtils.fromString(childBoundaryString));
            mediaType.set(PARAMETER_MAP_FIELD, paramMap);
        }
        writeBodyPartHeaders(writer, childPart);
        //Serialize nested parts
        if (childBoundaryString != null) {
            BArray nestedParts = (BArray) childPart.getNativeData(BODY_PARTS);
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
    @SuppressWarnings("unchecked")
    private void writeBodyPartHeaders(Writer writer, BObject bodyPart) throws IOException {
        BMap<BString, Object> httpHeaders = EntityHeaderHandler.getEntityHeaderMap(bodyPart);
        String contentType = MimeUtil.getContentTypeWithParameters(bodyPart);
        EntityHeaderHandler.addHeader(bodyPart, httpHeaders, MimeConstants.CONTENT_TYPE, contentType);
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (!contentDisposition.isEmpty()) {
            EntityHeaderHandler.addHeader(bodyPart, httpHeaders, MimeConstants.CONTENT_DISPOSITION, contentDisposition);
        }
        
        Object contentId = bodyPart.get(CONTENT_ID_FIELD);
        if (contentId != null && !contentId.toString().isEmpty()) {
            EntityHeaderHandler.addHeader(bodyPart, httpHeaders, MimeConstants.CONTENT_ID, contentId.toString());
        }
        Iterator<Map.Entry<BString, Object>> iterator = httpHeaders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BString, Object> entry = iterator.next();
            writer.write(String.valueOf(entry.getKey()));
            writer.write(COLON);
            writer.write(SPACE);
            BArray value = (BArray) entry.getValue();
            writer.write(String.valueOf(value.getBString(0)));
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
    private void writeBodyContent(OutputStream outputStream, BObject bodyPart) throws IOException {
        Object messageDataSource = EntityBodyHandler.getMessageDataSource(bodyPart);
        if (messageDataSource != null) {
            //TODO Recheck following logic
            if (messageDataSource instanceof String || messageDataSource instanceof Long ||
                    messageDataSource instanceof Double || messageDataSource instanceof Integer ||
                    messageDataSource instanceof Boolean) {
                outputStream.write(messageDataSource.toString().getBytes(Charset.defaultCharset()));
            } else {
                ((BRefValue) messageDataSource).serialize(outputStream);
            }
        } else {
            EntityBodyHandler.writeByteChannelToOutputStream(bodyPart, outputStream);
        }
    }

    public String stringValue(BLink parent) {
        return null;
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public Type getType() {
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
