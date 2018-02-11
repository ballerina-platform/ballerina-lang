/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.mime.util;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Set;

import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;

public class MultipartDataSource extends BallerinaMessageDataSource {

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
        //TODO: Get charset from content type and use that
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

                    //Write body headers
                    writeBodyPartHeaders(writer, bodyPart);
                    writeBodyContent(outputStream, bodyPart);
                }
            }
            writeFinalBoundaryString(writer, boundaryString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBodyPartHeaders(Writer writer, BStruct bodyPart) throws IOException {
        BMap<String, BValue> entityHeaders = (BMap) bodyPart.getRefField(ENTITY_HEADERS_INDEX);
        if (entityHeaders == null) {
            entityHeaders = new BMap<>();
        }
        setContentTypeHeader(bodyPart, entityHeaders);
        setContentDispositionHeader(bodyPart, entityHeaders);
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

    private void setContentTypeHeader(BStruct bodyPart, BMap<String, BValue> entityHeaders) {
        String contentType = MimeUtil.getContentTypeWithParameters(bodyPart);
        if (entityHeaders.keySet().contains(CONTENT_TYPE)) {
            BStringArray valueArray = (BStringArray) entityHeaders.get(CONTENT_TYPE);
            valueArray.add(valueArray.size(), contentType);
        } else {
            BStringArray valueArray = new BStringArray(new String[]{contentType});
            entityHeaders.put(CONTENT_TYPE, valueArray);
        }
    }

    private void setContentDispositionHeader(BStruct bodyPart, BMap<String, BValue> entityHeaders) {
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (contentDisposition != null) {
            if (entityHeaders.keySet().contains(CONTENT_DISPOSITION)) {
                BStringArray valueArray = (BStringArray) entityHeaders.get(CONTENT_DISPOSITION);
                valueArray.add(valueArray.size(), contentDisposition);
            } else {
                BStringArray valueArray = new BStringArray(new String[]{contentDisposition});
                entityHeaders.put(CONTENT_DISPOSITION, valueArray);
            }
        }
    }

    private void writeBodyContent(OutputStream outputStream, BStruct bodyPart) throws IOException {
        if (EntityBodyHandler.isContentInMemory(bodyPart)) {
            MessageDataSource messageDataSource = EntityBodyHandler.readMessageDataSource(bodyPart);
            if (messageDataSource != null) {
                messageDataSource.serializeData(outputStream);
            }
        } else if (EntityBodyHandler.isOverFlowDataNotNull(bodyPart)) {
            MimeUtil.writeFileToOutputStream(bodyPart, outputStream);
        }
    }

    private void writeFinalBoundaryString(Writer writer, String boundaryString) throws IOException {
        // Write the final boundary string
        writer.write(CRLF_POST_DASH);
        writer.write(boundaryString);
        writer.write(CRLF_PRE_DASH);
        writer.flush();
    }
}
