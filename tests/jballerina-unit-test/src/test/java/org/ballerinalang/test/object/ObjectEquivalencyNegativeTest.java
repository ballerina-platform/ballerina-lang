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
package org.ballerinalang.test.object;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined object types with attached functions in ballerina.
 */
public class ObjectEquivalencyNegativeTest {

    @Test(description = "Test equivalence of objects that are in the same package")
    public void testEquivalenceOfObjectsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-01-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 11);
        BAssertUtil.validateError(compileResult, 0,
                                  "incompatible types: 'employee01' cannot be cast to 'person01'", 24, 18);
        BAssertUtil.validateError(compileResult, 1,
                                  "incompatible types: 'employee02' cannot be cast to 'person02'", 51, 18);
//        BAssertUtil.validateError(compileResult, 2,
//                "incompatible types: 'employee04' cannot be cast to 'person04'", 108, 18);
        BAssertUtil.validateError(compileResult, 2,
                                  "incompatible types: 'employee05' cannot be cast to 'person05'", 145, 18);
        BAssertUtil.validateError(compileResult, 3,
                                  "incompatible types: 'employee06' cannot be cast to 'person06'", 175, 18);
        BAssertUtil.validateError(compileResult, 4,
                                  "incompatible types: 'employee08' cannot be cast to 'person08'", 284, 18);
        BAssertUtil.validateError(compileResult, 5,
                                  "incompatible types: 'employee09' cannot be cast to 'person09'", 341, 18);
        BAssertUtil.validateError(compileResult, 6,
                "incompatible types: expected 'ObjWithRemoteMethod', found 'NonClientObj'", 460, 29);
        BAssertUtil.validateError(compileResult, 7,
                "incompatible types: expected 'ObjWithRemoteMethod', found 'ClientObjWithoutRemoteMethod'", 465, 29);
        BAssertUtil.validateError(compileResult, 8,
                "incompatible types: expected 'ObjWithOnlyRemoteMethod', " +
                        "found 'ClientObjWithoutRemoteMethod'", 470, 33);
        BAssertUtil.validateError(compileResult, 9,
                "incompatible types: expected 'ObjWithOnlyRemoteMethod', found 'NonClientObj'", 475, 33);
        BAssertUtil.validateError(compileResult, 10,
                "incompatible types: expected 'NonClientObj', found 'ObjWithRemoteMethod'", 480, 22);
    }

    // TODO: 5/23/19 enabled this after ballerina-lang/issues/15384 is fixed
    @Test(description = "Test equivalence of objects that are in the same package from a third package",
          enabled = false)
    public void testEquivalenceOfObjectsInSamePackageFromDifferentPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-02-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: 'org.foo.bar:userBar' cannot be cast to 'org.foo:userFoo'", 11, 23);
        BAssertUtil.validateError(compileResult, 1,
                "incompatible types: 'org.foo.bar:BarObj' cannot be cast to 'org.foo:FooObj'", 17, 25);
    }
}
