/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Wait for any action related tests.
 */
public class WaitForOneActionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-for-one-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Wait for any actions test error count");
    }

    @Test
    public void waitTest1() {
        Object vals = BRunUtil.invoke(result, "waitTest1", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "7");
    }

    @Test
    public void waitTest2() {
        Object vals = BRunUtil.invoke(result, "waitTest2", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "true");
    }

    @Test
    public void waitTest3() {
        Object vals = BRunUtil.invoke(result, "waitTest3", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "hello foo");
    }

    @Test
    public void waitTest4() { // Need to check
        BArray vals = (BArray) BRunUtil.invoke(result, "waitTest4", new Object[0]);
        Assert.assertEquals(vals.size(), 3);
        Assert.assertEquals(vals.get(0).toString(), "22");
        Assert.assertEquals(vals.get(1).toString(), "hello bar");
        Assert.assertEquals(vals.get(2).toString(), "hello xyz");
    }

    @Test
    public void waitTest5() {
        Object vals = BRunUtil.invoke(result, "waitTest5", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "{\"fname\":\"foo\",\"lname\":\"bar\"}");
    }

    @Test
    public void waitTest6() {
        Object vals = BRunUtil.invoke(result, "waitTest6", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "99");
    }

    @Test
    public void waitTest7() {
        Object vals = BRunUtil.invoke(result, "waitTest7", new Object[0]);
        
        Assert.assertEquals(vals.toString(), "66");
    }

    @Test (expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest8() {
        BRunUtil.invoke(result, "waitTest8");
    }

    @Test
    public void waitTest9() {
        Object vals = BRunUtil.invoke(result, "waitTest9", new Object[0]);
        
        Assert.assertNull(vals);
    }

    @Test
    public void asyncObjectCreationTest() {
        BRunUtil.invoke(result, "asyncObjectCreationTest", new Object[0]);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
