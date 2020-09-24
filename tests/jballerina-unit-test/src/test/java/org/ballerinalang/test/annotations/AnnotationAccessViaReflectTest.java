/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.annotations;

//import org.ballerinalang.core.model.values.BBoolean;
//import org.ballerinalang.core.model.values.BValue;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.BRunUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test annotation access via the reflect module.
 *
 * @since 1.0
 */
@Test
public class AnnotationAccessViaReflectTest {

//    private CompileResult resultOne;
//
//    @BeforeClass
//    public void setup() {
//        resultOne = BCompileUtil.compile("test-src/annotations/annot_access_via_reflect.bal");
//        Assert.assertEquals(resultOne.getErrorCount(), 0);
//    }
//
//    @Test(dataProvider = "annotAccessTests")
//    public void testAnnotAccess(String testFunction) {
//        BValue[] returns = BRunUtil.invoke(resultOne, testFunction);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertSame(returns[0].getClass(), BBoolean.class);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @DataProvider(name = "annotAccessTests")
//    public Object[][] annotAccessTests() {
//        return new Object[][]{
//                { "testServiceAnnotAccess" },
//                { "testResourceAnnotAccess" }
//        };
//    }
}
