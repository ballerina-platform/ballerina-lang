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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test annotation declarations.
 *
 * @since 1.0
 */
public class AnnotationDeclarationTest {

    public static final String EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION = "expression is not a constant expression";

    @Test
    public void testSourceOnlyAnnotDeclWithoutSource() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/annotations/source_only_annot_without_source_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++,
                "annotation declaration with 'source' attach point(s) should be a 'const' declaration", 17, 1);
        BAssertUtil.validateError(compileResult, index++, "missing source keyword", 17, 30);
        BAssertUtil.validateError(compileResult, index++, "missing source keyword", 18, 28);
        BAssertUtil.validateError(compileResult, index++,
                "annotation declaration with 'source' attach point(s) should be a 'const' declaration", 19, 1);
        BAssertUtil.validateError(compileResult, index++, "missing source keyword", 19, 22);
        BAssertUtil.validateError(compileResult, index++, "missing source keyword", 20, 45);
        BAssertUtil.validateError(compileResult, index++, "missing source keyword", 21, 37);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testSourceAnnotDeclWithoutConst() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/annotations/source_annot_without_const_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 10);
        String errorMessage = "annotation declaration with 'source' attach point(s) should be a 'const' declaration";
        for (int index = 0; index < 9; index++) {
            BAssertUtil.validateError(compileResult, index, errorMessage, index + 17, 1);
        }
    }

    @Test
    public void testInvalidAnnotType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/annots_with_invalid_type.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0, "annotation declaration requires " +
                "a subtype of 'true', 'map<value:Cloneable>' or 'map<value:Cloneable>[]', but found 'int'", 17, 12);
        BAssertUtil.validateError(compileResult, 1,
                                  "annotation declaration requires a subtype of 'true', " +
                                          "'map<value:Cloneable>' or 'map<value:Cloneable>[]', " +
                                          "but found 'trueArray'", 22, 12);
    }

    @Test
    public void testAnnotWithInvalidConsts() {
        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/annots_with_invalid_consts.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 35, 14);
//        https://github.com/ballerina-platform/ballerina-lang/issues/38746
//        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 67, 14);
        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 80, 10);
        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 83, 16);
        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 86, 11);
        BAssertUtil.validateError(compileResult, index++, EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION, 86, 14);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}
