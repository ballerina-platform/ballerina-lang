/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.annotations;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test deprecation annotation.
 *
 * @since 1.2.0
 */
@Test(groups = { "disableOnOldParser" })
public class DeprecationAnnotationTest {

    @Test(description = "Test the deprecation annotation")
    public void testDeprecationAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 35);

        int i = 0;
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 24, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'CONST1' is deprecated", 24, 15);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'DummyObject' is deprecated", 42, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'DummyObject' is deprecated", 45, 23);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 45, 40);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'CONST1' is deprecated", 45, 62);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'func1()' is deprecated", 46, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'func3()' is deprecated", 54, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v1' is deprecated", 74, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v1' is deprecated", 86, 21);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v2' is deprecated", 87, 23);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v1' is deprecated", 90, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v2' is deprecated", 93, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v2' is deprecated", 96, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v4' is deprecated", 102, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v4' is deprecated", 109, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'x' is deprecated", 128, 12);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'y' is deprecated", 128, 16);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'z' is deprecated", 128, 20);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'x' is deprecated", 133, 12);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'y' is deprecated", 133, 16);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'z' is deprecated", 133, 20);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.fieldOne' is deprecated", 151, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.t' is deprecated", 152, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.fieldOne' is deprecated", 174, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.t' is deprecated", 175, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.fieldOne' is deprecated", 194, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'self.t' is deprecated", 195, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'add1(2, 3, 3)' is deprecated", 200, 14);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'add2(2, 4, 5)' is deprecated", 201, 14);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object1' is deprecated", 202, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'z' is deprecated", 213, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 216, 38);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 217, 5);
        BAssertUtil.validateWarning(compileResult, i, "usage of construct 'Foo' is deprecated", 222, 5);
    }

    @Test(description = "Test the deprecation annotation")
    public void testDeprecationAnnotationWithCRLF() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation_crlf.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 1);

        BAssertUtil.validateWarning(compileResult, 0, "usage of construct 'CONST1' is deprecated", 10, 30);
    }
}
