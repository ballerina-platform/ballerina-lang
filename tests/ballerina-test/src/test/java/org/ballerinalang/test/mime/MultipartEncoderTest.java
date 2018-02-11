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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimeTypeParseException;

public class MultipartEncoderTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        //Used only to get an instance of CompileResult.
        String sourceFilePath = "test-src/mime/dummy.bal";
        result = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test that the body parts get correctly encoded for multipart/mixed")
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
            List<MIMEPart> mimeParts= MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);

        } catch (MimeTypeParseException e) {
            e.printStackTrace();
        }
    }
}
