/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.balo.functions;

//import org.ballerinalang.core.model.values.BValue;
//import org.ballerinalang.test.balo.BaloCreator;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.BRunUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda in BALO.
 *
 * @since 0.975.0
 */
public class FunctionPointersTest {

//    CompileResult result;
//
//    @BeforeClass
//    public void setup() {
//        BaloCreator.cleanCacheDirectories();
//        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
//        result = BCompileUtil.compile("test-src/balo/test_balo/functions/test_global_function_pointers.bal");
//    }
//
//    @Test
//    public void testGlobalFP() {
//        BValue[] returns;
//        // testing function pointer.
//        returns = BRunUtil.invoke(result, "test1");
//        Assert.assertNotNull(returns);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertNotNull(returns[0]);
//        Assert.assertEquals(returns[0].stringValue(), "test1");
//    }
//
//    @Test
//    public void testGlobalFPAsLambda() {
//        BValue[] returns;
//        // lambda.
//        returns = BRunUtil.invoke(result, "test2");
//        Assert.assertNotNull(returns);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertNotNull(returns[0]);
//        Assert.assertEquals(returns[0].stringValue(), "test2true");
//    }
//
//    @Test
//    public void testGlobalFPAssignment() {
//        BValue[] returns;
//        // assign function pointer and invoke.
//        returns = BRunUtil.invoke(result, "test3");
//        Assert.assertNotNull(returns);
//        Assert.assertEquals(returns.length, 3);
//        Assert.assertNotNull(returns[0]);
//        Assert.assertEquals(returns[0].stringValue(), "test3");
//        Assert.assertNotNull(returns[1]);
//        Assert.assertEquals(returns[1].stringValue(), "test3");
//        Assert.assertNotNull(returns[2]);
//        Assert.assertEquals(returns[2].stringValue(), "3test");
//    }
//
//    @Test
//    public void testGlobalFPWithLocalFP() {
//        BValue[] returns;
//        // Check global and local variable.
//        returns = BRunUtil.invoke(result, "test5");
//        Assert.assertNotNull(returns);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertNotNull(returns[0]);
//        Assert.assertEquals(returns[0].stringValue(), "falsetest5");
//    }
//
//    @Test
//    public void testGlobalFPByAssigningLocalFP() {
//        BValue[] returns;
//        // assign local ref to global and invoke.
//        returns = BRunUtil.invoke(result, "test6");
//        Assert.assertNotNull(returns);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertNotNull(returns[0]);
//        Assert.assertEquals(returns[0].stringValue(), "truetest6");
//    }
//
//    @AfterClass
//    public void tearDown() {
//        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
//    }
}
