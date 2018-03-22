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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PermissionStoreTest {
    private static final Log log = LogFactory.getLog(PermissionStoreTest.class);
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "authorization", "permissionstore", BALLERINA_CONF);
        compileResult = BCompileUtil.compile(sourceRoot.resolve("permission-store.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating permission store")
    public void testCreatePermissionstore() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreatePermissionstore");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test(description = "Test case for reading groups of non-existing scope")
    public void testReadGroupsForNonExistingScope() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForNonExistingScope");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0].stringValue().isEmpty());
    }

    @Test(description = "Test case for reading groups of a scope")
    public void testReadGroupsForScope() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForScope");
        Assert.assertTrue(returns != null);
        BString groups = (BString) returns[0];
        log.info("Groups for scope: " + groups);
        Assert.assertNotNull(groups);
        Assert.assertEquals(groups.stringValue(), "xyz,pqr");
    }

    @Test(description = "Test case for reading groups of non-existing user")
    public void testReadGroupsForNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForNonExistingUser");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0].stringValue().isEmpty());
    }

    @Test(description = "Test case for reading groups of a user")
    public void testReadGroupsForUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadGroupsForUser");
        Assert.assertTrue(returns != null);
        BString groups = (BString) returns[0];
        log.info("Groups for user: " + groups);
        Assert.assertNotNull(groups);
        Assert.assertEquals(groups.stringValue(), "prq,lmn");
    }
}
