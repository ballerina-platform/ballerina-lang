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

package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.values.BStruct;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILENAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;

/**
 * Unit tests for multipart encoder.
 *
 * @since 0.962.0
 */
public class MultipartEncoderTest {
    private static final Logger log = LoggerFactory.getLogger(MultipartEncoderTest.class);

    private CompileResult result;

    @BeforeClass
    public void setup() {
        //Used only to get an instance of CompileResult.
        String sourceFilePath = "test-src/mime/dummy.bal";
        result = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test whether the body parts get correctly encoded for multipart/mixed")
    public void testMultipartWriterForMixed() {
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonBodyPart(result));
        bodyParts.add(Util.getXmlFilePart(result));
        bodyParts.add(Util.getTextBodyPart(result));
        bodyParts.add(Util.getBinaryFilePart(result));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(Util.getArrayOfBodyParts(bodyParts),
                multipartDataBoundary);
        multipartDataSource.serializeData(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);

        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        }
    }

    @Test(description = "Test whether the body parts get correctly encoded for any new multipart sub type")
    public void testMultipartWriterForNewSubTypes() {
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonBodyPart(result));
        bodyParts.add(Util.getXmlFilePart(result));
        bodyParts.add(Util.getTextBodyPart(result));
        bodyParts.add(Util.getBinaryFilePart(result));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(Util.getArrayOfBodyParts(bodyParts),
                multipartDataBoundary);
        multipartDataSource.serializeData(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/new-sub-type; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);

        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        }
    }

    @Test(description = "Test that the body part build the ContentDisposition struct properly for multipart/form-data")
    public void testContentDispositionForFormData() {
        BStruct bodyPart = Util.getEntityStruct(result);
        BStruct contentDispositionStruct = Util.getContentDispositionStruct(result);
        MimeUtil.setContentDisposition(contentDispositionStruct, bodyPart,
                "form-data; name=\"filepart\"; filename=\"file-01.txt\"");
        BStruct contentDisposition = (BStruct) bodyPart.getRefField(CONTENT_DISPOSITION_INDEX);
        Assert.assertEquals(contentDisposition.getStringField(CONTENT_DISPOSITION_FILENAME_INDEX),
                "\"file-01.txt\"");
        Assert.assertEquals(contentDisposition.getStringField(CONTENT_DISPOSITION_NAME_INDEX),
                "\"filepart\"");
        Assert.assertEquals(contentDisposition.getStringField(DISPOSITION_INDEX),
                "form-data");
    }
}
