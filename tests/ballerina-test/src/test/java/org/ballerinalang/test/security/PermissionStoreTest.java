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

package org.ballerinalang.test.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.config.utils.ConfigFileParserException;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PermissionStoreTest {
    private static final Log log = LogFactory.getLog(PermissionStoreTest.class);
    private CompileResult compileResult;

    private final String userDir = "user.dir";
    private String userDirectory;

    @BeforeClass
    public void setup() throws Exception {
        compileResult = BCompileUtil.compile("test-src/security/permission-store.bal");
        printDiagnostics(compileResult);
        userDirectory = System.getProperty(userDir);
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
    }

    private Map<String, String> getRuntimeProperties() {
        String ballerinaConf = "ballerina.conf";
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(ballerinaConf, getClass().getClassLoader()
                .getResource("datafiles/config/security/authorization/permissionstore/" + ballerinaConf).getPath());
        return runtimeConfigs;
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }

    @Test
    public void testCreatePermissionstore () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreatePermissionstore");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test
    public void testReadGroupsForNonExistingScope () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForNonExistingScope");
        Assert.assertTrue(returns != null);
        Assert.assertNull(returns[0].stringValue());
    }

    @Test
    public void testReadGroupsForScope () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForScope");
        Assert.assertTrue(returns != null);
        BString groups = (BString) returns[0];
        log.info("Groups for scope: " + groups);
        Assert.assertNotNull(groups);
        Assert.assertEquals(groups.stringValue(), "xyz,pqr");
    }

    @Test
    public void testReadGroupsForNonExistingUser () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForNonExistingUser");
        Assert.assertTrue(returns != null);
        Assert.assertNull(returns[0].stringValue());
    }

    @Test
    public void testReadGroupsForUser () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForUser");
        Assert.assertTrue(returns != null);
        BString groups = (BString) returns[0];
        log.info("Groups for user: " + groups);
        Assert.assertNotNull(groups);
        Assert.assertEquals(groups.stringValue(), "prq,lmn");
    }

    @AfterClass
    public void tearDown() throws ConfigFileParserException {
        System.setProperty(userDir, userDirectory);
    }
}
