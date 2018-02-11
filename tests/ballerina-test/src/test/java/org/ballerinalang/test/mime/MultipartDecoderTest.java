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

import io.netty.util.internal.StringUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;

public class MultipartDecoderTest {
    private CompileResult serviceResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/multipart-request.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test sending a multipart request as multipart/mixed with multiple body parts")
    public void testMultiplePartsForMixed() {
        String path = "/test/multipleparts";
        List<Header> headers = new ArrayList<>();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        headers.add(new Header(CONTENT_TYPE, "multipart/mixed; boundary=" + multipartDataBoundary));
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "bar" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "File 01" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";

        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers,
                multipartBody);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- bar -- File 01" + StringUtil.NEWLINE);
    }

    @Test(description = "Test sending a multipart request as multipart/form-data with multiple body parts")
    public void testMultiplePartsForFormData() {
        String path = "/test/multipleparts";
        List<Header> headers = new ArrayList<>();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        headers.add(new Header(CONTENT_TYPE, "multipart/form-data; boundary=" + multipartDataBoundary));
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"foo\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "bar" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"filepart\"; filename=\"file-01.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "File 01" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";

        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers,
                multipartBody);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- bar -- File 01" + StringUtil.NEWLINE);
    }
}
