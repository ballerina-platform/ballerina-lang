/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to cover while loop.
 *
 * @since 1.0.0
 */
public class WhileTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/while_loop.bal");
    }

    @Test(description = "Test breaking out of while loop using a global var, not that, this works fine " +
            "if the while loop body is bit complex, and this seems to be a bug in java itself, for example" +
            "commented out java code below this test case is failing(it doesn't return)")
    public void testBreakingWhileLoopWithGlobalVar() {
        BValue[] result = BRunUtil.invoke(compileResult, "breakWhileWithGlobalVar");
        Assert.assertTrue(result[0] instanceof BInteger);
        BInteger intVal = (BInteger) result[0];
        Assert.assertEquals(intVal.intValue(), 5);
    }

//    public static boolean classLevelVar = true;
//
//    public static int breakWhileUsingClassVar() {
//        (new Thread(() -> {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//            }
//            classLevelVar = false;
//        })).start();
//        while (classLevelVar) {
//            int a = 9;
//        }
//        // Below statement doesn't get hit, making the class level variable volatile solves the issue.
//        return 8;
//    }

}
