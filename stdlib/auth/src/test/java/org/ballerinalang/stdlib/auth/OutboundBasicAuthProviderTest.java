/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.auth;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration auth provider testcase.
 */
public class OutboundBasicAuthProviderTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        compileResult = BCompileUtil.compile(sourceRoot.resolve("outbound_basic_auth_provider_test.bal").toString());
    }

    @Test(description = "Test case for creating outbound basic auth provider")
    public void testCreateOutboundAuthProvider() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateOutboundBasicAuthProvider");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BMap);
    }

    @Test(description = "Test case for successful token generation")
    public void testTokenGeneration() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTokenGeneration");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful token generation when username is empty")
    public void testTokenGenerationWithEmptyUsername() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTokenGenerationWithEmptyUsername");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful token generation when password is empty")
    public void testTokenGenerationWithEmptyPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTokenGenerationWithEmptyPassword");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful token generation when username and password are empty")
    public void testTokenGenerationWithEmptyUsernameAndEmptyPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTokenGenerationWithEmptyUsernameAndEmptyPassword");
        assertFailureOfResults(returns);
    }

    private void assertSuccessOfResults(BValue[] returns) {
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BString && returns[0].stringValue().equals("dG9tOjEyMw=="));
    }

    private void assertFailureOfResults(BValue[] returns) {
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BError);
    }
}
