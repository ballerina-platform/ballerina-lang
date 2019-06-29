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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test annotation declarations.
 *
 * @since 1.0
 */
public class AnnotationDeclarationTest {

    @Test
    public void testSourceOnlyAnnotDeclWithoutSource() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/annotations/source_only_annot_without_source_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 6);
        BAssertUtil.validateError(compileResult, 0,
                                  "mismatched input 'var'. expecting {'service', 'resource', 'function', " +
                                          "'object', 'parameter', 'source', 'type', 'return'}", 17, 30);
        BAssertUtil.validateError(compileResult, 1,
                                  "mismatched input 'annotation'. expecting {'service', 'resource', 'function', " +
                                          "'object', 'parameter', 'source', 'type', 'return'}", 18, 28);
        BAssertUtil.validateError(compileResult, 2,
                                  "mismatched input 'const'. expecting {'service', 'resource', 'function', " +
                                          "'object', 'parameter', 'source', 'type', 'return'}", 19, 22);
        BAssertUtil.validateError(compileResult, 4,
                                  "mismatched input 'external'. expecting {'service', 'resource', 'function', " +
                                          "'object', 'parameter', 'source', 'type', 'return'}", 20, 45);
        BAssertUtil.validateError(compileResult, 5,
                                  "mismatched input 'listener'. expecting {'service', 'resource', 'function', " +
                                          "'object', 'parameter', 'source', 'type', 'return'}", 21, 37);
    }

    @Test
    public void testSourceAnnotDeclWithoutConst() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/annotations/source_annot_without_const_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 10);
        String errorMessage = "annotation definition with 'source' attach point(s) should be a 'const' definition";
        for (int index = 0; index < 9; index++) {
            BAssertUtil.validateError(compileResult, index, errorMessage, index + 17, 1);
        }
    }
}
