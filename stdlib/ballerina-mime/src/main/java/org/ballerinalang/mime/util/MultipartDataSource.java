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

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Set;

import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION;
import static org.ballerinalang.mime.util.Constants.CONTENT_ID;
import static org.ballerinalang.mime.util.Constants.CONTENT_ID_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;

/**
 * Act as multipart encoder.
 *
 * @since 0.963.0
 */
public class MultipartDataSource extends BallerinaMessageDataSource {
    private static final Logger log = LoggerFactory.getLogger(MultipartDataSource.class);

    private BRefValueArray bodyParts;
    private String boundaryString;
    private static final String DASH_BOUNDARY = "--";
    private static final String CRLF_POST_DASH = "\r\n--";
    private static final String CRLF_PRE_DASH = "--\r\n";
    private static final String CRLF = "\r\n";
    private static final char COMMA = ',';
    private static final char COLON = ':';
    private static final char SPACE = ' ';

    public MultipartDataSource(BRefValueArray bodyParts, String boundaryString) {
        this.bodyParts = bodyParts;
        this.boundaryString = boundaryString;
    }

    @Override
    public void serializeData(OutputStream outputStream) {
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.defaultCharset()));
        try {
            if (bodyParts != null) {
                boolean isFirst = true;
                for (int i = 0; i < bodyParts.size(); i++) {

                    BStruct bodyPart = (BStruct) bodyParts.get(i);
                    // Write leading boundary string
                    if (isFirst) {
                        isFirst = false;
                        writer.write(DASH_BOUNDARY);

                    } else {
                        writer.write(CRLF_POST_DASH);
                    }
                    writer.write(boundaryString);
                    writer.write(CRLF);

                    writeBodyPartHeaders(writer, bodyPart);
                    writeBodyContent(outputStream, bodyPart);
                }
            }
            writeFinalBoundaryString(writer, boundaryString);
        } catch (IOException e) {
            log.error("Error occurred while writing body parts to outputstream", e.getMessage());
        }
    }

    /**
     * Write body part headers to output stream.
     *
     * @param writer   Represent the outputstream writer
     * @param bodyPart Represent ballerina body part
     * @throws IOException When an error occurs while writing body part headers
     */
    private void writeBodyPartHeaders(Writer writer, BStruct bodyPart) throws IOException {
        BMap<String, BValue> entityHeaders = bodyPart.getRefField(ENTITY_HEADERS_INDEX) != null ?
                (BMap) bodyPart.getRefField(ENTITY_HEADERS_INDEX) : null;
        if (entityHeaders == null) {
            entityHeaders = new BMap<>();
        }
        setContentTypeHeader(bodyPart, entityHeaders);
        setContentDispositionHeader(bodyPart, entityHeaders);
        setContentIdHeader(bodyPart, entityHeaders);

        Set<String> keys = entityHeaders.keySet();
        for (String key : keys) {
            BStringArray headerValues = (BStringArray) entityHeaders.get(key);
            writer.write(key);
            writer.write(COLON);
            boolean first = true;
            for (int j = 0; j < headerValues.size(); j++) {
                if (first) {
                    writer.write(SPACE);
                    first = false;
                } else {
                    writer.write(COMMA);
                }
                writer.write(headerValues.get(j));
            }
            writer.write(CRLF);
        }
        // Mark the end of the headers for this body part
        writer.write(CRLF);
        writer.flush();
    }

    /**
     * Add content type info while is in MediaType struct as an entity header.
     *
     * @param bodyPart      Represent a ballerina body part
     * @param entityHeaders Map of entity headers
     */
    private void setContentTypeHeader(BStruct bodyPart, BMap<String, BValue> entityHeaders) {
        String contentType = MimeUtil.getContentTypeWithParameters(bodyPart);
        HeaderUtil.addToEntityHeaders(entityHeaders, CONTENT_TYPE, contentType);
    }

    /**
     * Add content disposition info while is in ContentDisposition struct as an entity header.
     *
     * @param bodyPart      Represent a ballerina body part
     * @param entityHeaders Map of entity headers
     */
    private void setContentDispositionHeader(BStruct bodyPart, BMap<String, BValue> entityHeaders) {
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (MimeUtil.isNotNullAndEmpty(contentDisposition)) {
            HeaderUtil.addToEntityHeaders(entityHeaders, CONTENT_DISPOSITION, contentDisposition);
        }
    }

    /**
     * Add content id as an entity header.
     *
     * @param bodyPart      Represent a ballerina body part
     * @param entityHeaders Map of entity headers
     */
    private void setContentIdHeader(BStruct bodyPart, BMap<String, BValue> entityHeaders) {
        String contentId = bodyPart.getStringField(CONTENT_ID_INDEX);
        if (MimeUtil.isNotNullAndEmpty(contentId)) {
            HeaderUtil.addToEntityHeaders(entityHeaders, CONTENT_ID, contentId);
        }
    }

    /**
     * Write body part content to outputstream.
     *
     * @param outputStream Represent an outputstream
     * @param bodyPart     Represent a ballerina body part
     * @throws IOException When an error occurs while writing body content
     */
    private void writeBodyContent(OutputStream outputStream, BStruct bodyPart) throws IOException {
        MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(bodyPart);
        if (messageDataSource != null) {
            messageDataSource.serializeData(outputStream);
        } else {
            EntityBodyHandler.writeByteChannelToOutputStream(bodyPart, outputStream);
        }
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
}
