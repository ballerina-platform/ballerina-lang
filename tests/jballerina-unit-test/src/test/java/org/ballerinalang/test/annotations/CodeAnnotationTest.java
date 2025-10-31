/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.annotations;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative tests for the code annotation.
 *
 * @since 2201.13.0
 */
public class CodeAnnotationTest {

    @Test
    public void testExternalDependencyAnnotation() {
        CompileResult compileResult =
                BCompileUtil.compileWithoutInitInvocation("test-src/annotations/code_annotation_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++,
                "annotation value expected for annotation of record type " +
                        "'record {| string prompt; |}' with required fields", 17, 17);
        BAssertUtil.validateError(compileResult, i++,
                "missing non-defaultable required record field 'prompt'", 19, 31);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'string', found 'int'", 22, 13);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
