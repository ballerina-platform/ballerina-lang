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

package org.ballerinalang.test.service.http2;

import java.nio.file.Path;
import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Testing Mutual SSL.
 */
@Test(groups = "http2-test")
public class Http2MutualSslTestCase extends Http2BaseTest {

    @Test(description = "Test mutual ssl with http2 prior knowledge")
    public void testMutualSSL() throws Exception {
        String serverResponse = "Passed";
        Path balFilePath = Paths.get("src", "test", "resources", "ssl",
                "http2_mutual_ssl_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        String keystorePath = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String truststorePath = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        BValue[] params = {new BString(keystorePath), new BString(truststorePath)};
        BValue[] responses = BRunUtil.invoke(result, "testMutualTls", params);
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        BString responseValues = (BString) responses[0];
        Assert.assertEquals(responseValues.stringValue(), serverResponse);
    }
}
