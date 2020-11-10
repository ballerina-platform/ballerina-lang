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

package org.ballerinalang.stdlib.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Testcase for complex key configuration.
 */
public class ComplexConfigKeyTest {
    private static final String COMPLEX_BALLERINA_CONF = "complex-key-ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws IOException {
        resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        Path ballerinaConfPath = Paths.get(resourceRoot, "datafiles", COMPLEX_BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("complex_keys.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "test dotted table header with quoted entries")
    public void testDottedTableHeaderWithQuotedEntries() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDottedTableHeaderWithQuotedEntries");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "xxx,yyy");
    }

    @Test(description = "test colon separated table header")
    public void testColonSeparatedTableHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testColonSeparatedTableHeader");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "xxx,yyy,abc");
    }

    @Test(description = "test dotted key with quoted entries")
    public void testDottedKeyWithQuotedEntries() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDottedKeyWithQuotedEntries");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "zzzz");
    }

    @Test(description = "test slash separated key")
    public void testSlashSeparatedKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSlashSeparatedKey");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "abc123");
    }

    @Test(description = "test slash separated header and key")
    public void testSlashSeparatedHeaderAndKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSlashSeparatedHeaderAndKey");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "lmn789");
    }

    @Test(description = "test simple key")
    public void testSimpleKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleKey");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertEquals(returns[0].stringValue(), "testVal");
    }
}
