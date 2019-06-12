/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.entity;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test mime with http.
 *
 * @since 0.970.0
 */
public class MimeWithHttpTest {
    private static final Logger LOG = LoggerFactory.getLogger(MimeWithHttpTest.class);

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/entity/mime-with-http.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    //TODO:Enable this once the IO tests are moved to its module
   /* @Test(description = "When the payload exceeds 2MB check whether the response received back matches  " +
            "the original content length")
    public void testLargePayload() {
        String path = "/test/largepayload";
        try {
            ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/fileThatExceeds2MB.txt");
            Channel channel = new MockByteChannel(byteChannel);
            CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
            String responseValue = characterChannel.readAll();
            characterChannel.close();
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST",
                    responseValue);
            HttpCarbonMessage response = Services.invokeNew(serviceResult, "mockEP", cMsg);
            Assert.assertNotNull(response, "Response message not found");

            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, outputStream);

            //length of the output stream depends on the OS because of the line terminating
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
                Assert.assertEquals(outputStream.size(), 2355885);
            } else {
                Assert.assertEquals(outputStream.size(), 2323779);
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error occurred in testLargePayload", e.getMessage());
        }
    }*/

    @Test(description = "Set header to entity and access it via Request")
    public void testHeaderWithRequest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHeaderWithRequest", args);
        Assert.assertEquals(returns.length, 1, "One value should be returned from this test");
        Assert.assertEquals(returns[0].stringValue(), "123Basicxxxxxx");
    }

    @Test(description = "Set header to entity and access it via Response")
    public void testHeaderWithResponse() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHeaderWithRequest", args);
        Assert.assertEquals(returns.length, 1, "One value should be returned from this test");
        Assert.assertEquals(returns[0].stringValue(), "123Basicxxxxxx");
    }
}
