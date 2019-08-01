/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.grpc.sample;

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test mutual ssl with certificates and keys.
 */
@Test(groups = "grpc-test")
public class GrpcMutualSslWithCertsTest extends GrpcBaseTest {
    private CompileResult result;
    private static final ConfigRegistry registry = ConfigRegistry.getInstance();

    @BeforeClass
    public void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test
    public void testMutualSSLWithcerts() throws IOException {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients", "10_grpc_ssl_client.bal");
        String privateKey = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                "public.crt").toAbsolutePath().toString());
        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("client.certificate.key", privateKey);
        runtimeParams.put("client.public.cert", publicCert);
        registry.initRegistry(runtimeParams, null, null);
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "Hello WSO2";
        BValue[] responses = BRunUtil.invoke(result, "testUnarySecuredBlockingWithCerts", new Object[] {});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        BString responseValues = (BString) responses[0];
        Assert.assertEquals(responseValues.stringValue(), serverMsg);
    }
}

