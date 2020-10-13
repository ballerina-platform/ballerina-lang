/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static io.ballerina.runtime.XMLFactory.isEqual;
import static io.ballerina.runtime.XMLFactory.parse;

/**
 * Test case for XML Serialization.
 */
@Test(groups = "http-test")
public class SerializeComplexXmlTestCase extends HttpBaseTest {

    @Test
    public void testXmlSerialization() throws IOException {
        int servicePort = 9250;
        HttpResponse response = HttpClientRequest.doGetAndPreserveNewlineInResponseData(
                serverInstance.getServiceURLHttp(servicePort, "serialize/xml"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                            TestConstant.CONTENT_TYPE_XML, "Content-Type mismatched");
        Assert.assertTrue(isEqual(parse(response.getData()), parse(getInputStream())),
                          "Message content mismatched");
    }

    public InputStream getInputStream() throws IOException {
        String filePath = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "http", "ComplexTestXmlSample.xml").toAbsolutePath().toString());
        return new FileInputStream(filePath);
    }
}
