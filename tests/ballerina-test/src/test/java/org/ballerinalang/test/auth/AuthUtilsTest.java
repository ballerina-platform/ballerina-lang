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

package org.ballerinalang.test.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthUtilsTest {

    private static final Log log = LogFactory.getLog(AuthUtilsTest.class);
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths.get(resourceRoot, "datafiles", "config", "auth", "caching", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("auth-utils.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating a cache which is disabled from configs")
    public void testCreateDisabledBasicAuthCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateDisabledBasicAuthCache");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertTrue(returns[0] == null);
    }

    @Test(description = "Test case for creating a cache")
    public void testCreateAuthzCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateAuthzCache");
        Assert.assertTrue(returns != null);
        // authz cache is enabled, hence should not be null
        Assert.assertTrue(returns[0] != null);
    }

    @Test(description = "Test case for extracting credentials from invalid header")
    public void testExtractBasicAuthCredentialsFromInvalidHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthCredentialsFromInvalidHeader");
        Assert.assertTrue(returns != null);
        // an error should be returned
        Assert.assertEquals(returns[0].getType().getName(), "error");
    }

    @Test(description = "Test case for extracting credentials from basic auth header")
    public void testExtractBasicAuthCredentials() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthCredentials");
        Assert.assertTrue(returns != null);
        // username and password should ne returned
        Assert.assertEquals(returns[0].stringValue(), "isuru");
        Assert.assertEquals(returns[1].stringValue(), "xxx");
    }
}
