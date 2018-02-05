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

import io.netty.util.internal.PlatformDependent;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.BlobDataSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

public class MultipartDataSource extends BallerinaMessageDataSource {

    private BRefValueArray bodyParts;

    public MultipartDataSource(BRefValueArray bodyParts) {
        this.bodyParts = bodyParts;
    }

    @Override
    public void serializeData(OutputStream outputStream) {
        //TODO: Get charset from content type and use that
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.defaultCharset()));
        String boundaryString = getNewMultipartDelimiter();
        if (bodyParts != null) {
            boolean isFirst = true;
            for (int i = 0; i < bodyParts.size(); i++) {
                try {
                    BStruct bodyPart = (BStruct) bodyParts.get(i);
               /* MimeUtil.encodeBodyPart(nettyEncoder, outboundRequest.getNettyHttpRequest(),
                        bodyPart);*/
                    // Write the leading boundary string
                    if (isFirst) {
                        isFirst = false;
                        writer.write("--");

                    } else {
                        writer.write("\r\n--");
                    }
                    writer.write(boundaryString);
                    writer.write("\r\n");

                    //TODO:Write body headers
                    //...
                    // Mark the end of the headers for this body part
                    writer.write("\r\n");
                    writer.flush();

                    writeBodyContent(writer, outputStream, bodyPart);
                    writeFinalBoundaryString(writer, boundaryString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeBodyContent(Writer writer, OutputStream outputStream, BStruct bodyPart) throws IOException {
        String baseType = MimeUtil.getContentType(bodyPart);
        if (baseType != null) {
            switch (baseType) {
                case TEXT_PLAIN:
                    String textPayload = MimeUtil.getTextPayload(bodyPart);
                    writer.write(textPayload);
                case APPLICATION_JSON:
                    BJSON jsonPayload = MimeUtil.getJsonPayload(bodyPart);
                    jsonPayload.serializeData(outputStream);
                    break;
                case APPLICATION_XML:
                    BXML xmlPayload = MimeUtil.getXmlPayload(bodyPart);
                    xmlPayload.serializeData(outputStream);
                    break;
                default:
                    byte[] binaryPayload = MimeUtil.getBinaryPayload(bodyPart);
                    if (binaryPayload != null) {
                        new BlobDataSource(binaryPayload).serializeData(outputStream);
                    }
            }
        } else {
            byte[] binaryPayload = MimeUtil.getBinaryPayload(bodyPart);
            if (binaryPayload != null) {
                new BlobDataSource(binaryPayload).serializeData(outputStream);
            }
        }
    }

    private void writeFinalBoundaryString(Writer writer, String boundaryString) throws IOException {
        // Write the final boundary string
        writer.write("\r\n--");
        writer.write(boundaryString);
        writer.write("--\r\n");
        writer.flush();
    }

    private static String getNewMultipartDelimiter() {
        return Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
    }
}
