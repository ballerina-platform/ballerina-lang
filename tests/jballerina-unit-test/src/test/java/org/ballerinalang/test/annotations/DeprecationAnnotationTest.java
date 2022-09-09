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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test deprecation annotation.
 *
 * @since 1.2.0
 */
@Test
public class DeprecationAnnotationTest {

    @Test(description = "Test the deprecation annotation")
    public void testDeprecationAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 48);

        int i = 0;
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 24, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'CONST1' is deprecated", 24, 15);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'DummyObject' is deprecated", 42, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'DummyObject' is deprecated", 45, 23);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 45, 40);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'CONST1' is deprecated", 45, 62);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'func1' is deprecated", 46, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'func3' is deprecated", 54, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v1' is deprecated", 74, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v1' is deprecated", 86, 16);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'v2' is deprecated", 87, 18);
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
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object1.fieldOne' is deprecated", 151, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object1.t' is deprecated", 152, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object2.fieldOne' is deprecated", 174, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object2.t' is deprecated", 175, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object3.fieldOne' is deprecated", 194, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object3.t' is deprecated", 195, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'add1' is deprecated", 200, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'add2' is deprecated", 201, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Object1' is deprecated", 202, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'z' is deprecated", 213, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 216, 38);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 217, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Foo' is deprecated", 222, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct '$anonType$_0' is deprecated", 230, 27);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'myAnnot' is deprecated", 240, 1);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'myAnnot' is deprecated", 247, 9);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Person.name' is deprecated", 266, 16);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'MyObject' is deprecated", 274, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'id' is deprecated", 279, 20);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'MyObject.id' is deprecated", 283, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'MyObject.getId' is deprecated", 284, 13);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Person' is deprecated", 286, 5);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Person' is deprecated", 286, 19);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Person.name' is deprecated", 287, 16);
        BAssertUtil.validateWarning(compileResult, i++, "usage of construct 'Person.getName' is deprecated", 288, 16);
        BAssertUtil.validateWarning(compileResult, i, "usage of construct 'myFunction' is deprecated", 298, 5);

    }

    @Test(description = "Test the deprecation annotation")
    public void testDeprecationAnnotationWithCRLF() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation_crlf.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 1);

        BAssertUtil.validateWarning(compileResult, 0, "usage of construct 'CONST1' is deprecated", 10, 30);
    }

    @Test
    public void testDeprecatedConstructUsageAtRuntimeWithWarning() {
        CompileResult result = BCompileUtil.compile("test-src/annotations/deprecated_construct_usage_with_warning.bal");

        Assert.assertEquals(result.getWarnCount(), 1);
        BAssertUtil.validateWarning(result, 0, "usage of construct 'Foo' is deprecated", 23, 1);

        BRunUtil.invoke(result, "testDeprecatedConstructUsageAtRuntimeWithWarning");
    }
}
