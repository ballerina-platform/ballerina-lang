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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined object types with attached functions in ballerina.
 */
public class ObjectEquivalencyNegativeTest {

    @Test(description = "Test equivalence of objects that are in the same package")
    public void testEquivalenceOfObjectsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-01-negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee01' cannot be cast to 'person01'", 24, 18);
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee02' cannot be cast to 'person02'", 51, 18);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: 'employee04' cannot be cast to 'person04'", 108, 18);
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee05' cannot be cast to 'person05'", 145, 18);
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee06' cannot be cast to 'person06'", 175, 18);
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee08' cannot be cast to 'person08'", 284, 18);
        BAssertUtil.validateError(compileResult, i++,
                                  "incompatible types: 'employee09' cannot be cast to 'person09'", 341, 18);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'ObjWithRemoteMethod', found 'NonClientObj'", 460, 29);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'ObjWithRemoteMethod', found 'ClientObjWithoutRemoteMethod'", 465, 29);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'ObjWithOnlyRemoteMethod', " +
                        "found 'ClientObjWithoutRemoteMethod'", 470, 33);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'ObjWithOnlyRemoteMethod', found 'NonClientObj'", 475, 33);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'NonClientObj', found 'ObjWithRemoteMethod'", 480, 22);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @Test(description = "Test equivalence of objects that are in the same package from a third package")
    public void testEquivalenceOfObjectsInSamePackageFromDifferentPackage() {
        BCompileUtil.compileAndCacheBala("test-src/object/ObjectProject");
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-02-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: 'testorg/objectpkg.org_foo_bar:1.0.0:userBar' cannot be cast to " +
                        "'testorg/objectpkg.org_foo:1.0.0:userFoo'", 11, 23);
        BAssertUtil.validateError(compileResult, 1,
                "incompatible types: 'testorg/objectpkg.org_foo_bar:1.0.0:BarObj' cannot be cast to " +
                        "'testorg/objectpkg.org_foo:1.0.0:FooObj'", 17, 25);
    }
}
