/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
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
 * Test cases for looking up env vars when the specified config key cannot be found in the registry. The env vars for
 * these tests are set in the surefire configuration in the POM file.
 */
public class EnvVarConfigTest {

    private static final ConfigRegistry registry = ConfigRegistry.getInstance();
    private CompileResult compileResult;

    @BeforeClass
    public void setup() throws IOException {
        registry.initRegistry(null, null, null); // empty registry
        Path serviceBalPath = Paths.get("src", "test", "resources", "test-src",
                "config.bal");
        compileResult = BCompileUtil.compile(serviceBalPath.toAbsolutePath().toString());
    }

    @Test
    public void testStringEnvVarLookup() {
        BString key = new BString("user.name");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "b7auser");
    }

    @Test
    public void testStringEnvVarLookup2() {
        BString key = new BString("password");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "b7apw");
    }

    @Test
    public void testIntEnvVarLookup() {
        BString key = new BString("hello.http.port");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsInt", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 5656);
    }

    @Test
    public void testFloatEnvVarLookup() {
        BString key = new BString("hello.eviction.fac");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 0.2333333);
    }

    @Test
    public void testBooleanEnvVarLookup() {
        BString key = new BString("hello.cache.enabled");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue());
    }

    @Test
    public void testNonExistentEnvVarLookupForStrings() {
        BString key = new BString("non.existent.env.var");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testNonExistentEnvVarLookupForInts() {
        BString key = new BString("non.existent.env.var");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsInt", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 0);
    }

    @Test
    public void testNonExistentEnvVarLookupForFloat() {
        BString key = new BString("non.existent.env.var");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 0.0);
    }

    @Test
    public void testNonExistentEnvVarLookupForBoolean() {
        BString key = new BString("non.existent.env.var");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*'string' value 'b7auser' cannot be converted to 'int'.*")
    public void testInvalidIntEnvVarLookup() {
        BString key = new BString("user.name");
        BRunUtil.invoke(compileResult, "testGetAsInt", new BValue[]{key});
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*'string' value 'b7auser' cannot be converted to 'float'.*")
    public void testInvalidFloatEnvVarLookup() {
        BString key = new BString("user.name");
        BRunUtil.invoke(compileResult, "testGetAsFloat", new BValue[]{key});
    }

    @Test
    public void testInvalidBooleanEnvVarLookup() {
        BString key = new BString("user.name");
        BValue[] inputArg = {key};

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue());
    }
}
