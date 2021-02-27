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

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing SSL protocols.
 */
@Test(groups = "http-test")
public class SslProtocolTest extends BaseTest {
    private static final ConfigRegistry registry = ConfigRegistry.getInstance();

    @Test(description = "Test ssl protocols")
    public void testSslProtocols() throws Exception {
        String clientLog = "SSL connection failed";

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "ssl" + File.separator + "ssl_protocol_client.bal").getAbsolutePath();

        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());

        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("truststore", trustStore);
        registry.initRegistry(runtimeParams, null, null);
        CompileResult result = BCompileUtil.compile(balFile);

        BValue[] responses = BRunUtil.invoke(result, "testSslProtocol", new Object[] {});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertTrue(responses[0].stringValue().contains(clientLog));
    }
}
