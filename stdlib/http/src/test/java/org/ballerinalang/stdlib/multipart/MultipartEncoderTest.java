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

package org.ballerinalang.stdlib.multipart;

import io.ballerina.runtime.api.values.BObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.Base64ByteChannel;
import org.ballerinalang.stdlib.io.utils.Base64Wrapper;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.MultipartUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILENAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.net.http.ValueCreatorUtils.createEntityObject;
import static org.ballerinalang.stdlib.mime.Util.getContentDispositionStruct;
import static org.ballerinalang.stdlib.mime.Util.getMultipartEntity;
import static org.ballerinalang.stdlib.mime.Util.getNestedMultipartEntity;
import static org.ballerinalang.stdlib.mime.Util.validateBodyPartContent;
import static org.ballerinalang.stdlib.utils.MultipartUtils.createNestedPartRequest;

/**
 * Unit tests for multipart encoder.
 *
 * @since 0.963.0
 */
public class MultipartEncoderTest {
    private static final Logger log = LoggerFactory.getLogger(MultipartEncoderTest.class);

    private CompileResult compileResult;
    private static final int EP_PORT = 9090;

    @BeforeClass
    public void setup() {
        String sourceFilePathForServices = "test-src/multipart/multipart-response.bal";
        BCompileUtil.compile(sourceFilePathForServices);
        compileResult = BCompileUtil.compile("test-src/multipart/bytechannel-base64.bal");
    }

    @Test(description = "Test whether the body parts get correctly encoded for multipart/mixed")
    public void testMultipartWriterForMixed() {
        BObject multipartEntity = getMultipartEntity();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(multipartEntity, multipartDataBoundary);
        multipartDataSource.serialize(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);
            BObject bodyPart = createEntityObject();
            validateBodyPartContent(mimeParts, bodyPart);
        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred while decoding binary part", e.getMessage());
        }
    }

    @Test(description = "Test whether the body parts get correctly encoded for any new multipart sub type")
    public void testMultipartWriterForNewSubTypes() {
        BObject multipartEntity = getMultipartEntity();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(multipartEntity, multipartDataBoundary);
        multipartDataSource.serialize(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/new-sub-type; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);
            BObject bodyPart = createEntityObject();
            validateBodyPartContent(mimeParts, bodyPart);
        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred while decoding binary part", e.getMessage());
        }
    }

    @Test(description = "Test whether the nested body parts within a multipart entity can be properly encoded")
    public void testNestedParts() {
        BObject nestedMultipartEntity = getNestedMultipartEntity();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(nestedMultipartEntity, multipartDataBoundary);
        multipartDataSource.serialize(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);
            for (MIMEPart mimePart : mimeParts) {
                testNestedPartContent(mimePart);
            }
        } catch (MimeTypeParseException | IOException e) {
            log.error("Error occurred while testing encoded nested parts", e.getMessage());
        }
    }

    /**
     * When nested parts have been properly encoded, decoding should work as it should.
     *
     * @param mimePart MIMEPart that contains nested parts
     * @throws MimeTypeParseException When an error occurs while parsing body content
     * @throws IOException            When an error occurs while validating body content
     */
    private void testNestedPartContent(MIMEPart mimePart) throws MimeTypeParseException, IOException {
        List<MIMEPart> nestedParts = MultipartDecoder.decodeBodyParts(mimePart.getContentType(),
                mimePart.readOnce());
        Assert.assertEquals(nestedParts.size(), 4);
        BObject ballerinaBodyPart = createEntityObject();
        validateBodyPartContent(nestedParts, ballerinaBodyPart);
    }

    @Test(description = "Test whether the body part builds the ContentDisposition struct properly for " +
            "multipart/form-data")
    public void testContentDispositionForFormData() {
        BObject bodyPart = createEntityObject();
        BObject contentDispositionStruct = getContentDispositionStruct();
        MimeUtil.setContentDisposition(contentDispositionStruct, bodyPart,
                                   "form-data; name=\"filepart\"; filename=\"file-01.txt\"");
        BObject contentDisposition =
                (BObject) bodyPart.get(CONTENT_DISPOSITION_FIELD);
        Assert.assertEquals(contentDisposition.get(CONTENT_DISPOSITION_FILENAME_FIELD).toString(), "file-01.txt");
        Assert.assertEquals(contentDisposition.get(CONTENT_DISPOSITION_NAME_FIELD).toString(), "filepart");
        Assert.assertEquals(contentDisposition.get(DISPOSITION_FIELD).toString(), "form-data");
    }

    @Test(description = "Test whether the encoded body parts can be sent through Response, with a given boundary")
    public void testMultipartsInOutResponse() {
        String path = "/multipart/encode_out_response";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(EP_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    "e3a0b9ad7b4e7cdb", inputStream);
            Assert.assertEquals(mimeParts.size(), 4);
            BObject bodyPart = createEntityObject();
            validateBodyPartContent(mimeParts, bodyPart);
        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred while decoding binary part", e.getMessage());
        }
    }

    @Test(description = "Retrieve body parts from the Request and send it across Response")
    public void testNestedPartsInOutResponse() {
        String path = "/multipart/nested_parts_in_outresponse";
        HTTPTestRequest inRequestMsg = createNestedPartRequest(path);
        HttpCarbonMessage response = Services.invoke(EP_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts(inRequestMsg.getHeader(HttpHeaderNames.
                            CONTENT_TYPE.toString()),
                    inputStream);
            Assert.assertEquals(mimeParts.size(), 2);
            List<MIMEPart> childParts = MultipartDecoder.decodeBodyParts(mimeParts.get(1).getContentType(),
                    mimeParts.get(1).readOnce());
            Assert.assertEquals(childParts.size(), 2);
        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        }
    }

    @Test(enabled = false)
    public void testBase64EncodeByteChannel() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BObject byteChannelStruct = MultipartUtils.getByteChannelStruct();
        InputStream inputStream = new ByteArrayInputStream("Hello Ballerina".getBytes());
        Base64ByteChannel base64ByteChannel = new Base64ByteChannel(inputStream);
        byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new Base64Wrapper(base64ByteChannel));
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64EncodeByteChannel",
                                                new Object[]{ byteChannelStruct });
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        BMap<String, BValue> decodedByteChannel = (BMap<String, BValue>) returnValues[0];
        Channel byteChannel = (Channel) decodedByteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        try {
            Assert.assertEquals(StringUtils.getStringFromInputStream(byteChannel.getInputStream()), expectedValue);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
