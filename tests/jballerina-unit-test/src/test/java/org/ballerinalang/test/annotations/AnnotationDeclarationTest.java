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

    @Test(groups = {"disableOnOldParser"})
    public void testSourceOnlyAnnotDeclWithoutSource() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/annotations/source_only_annot_without_source_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 7);
        BAssertUtil.validateError(compileResult, 0,
                "annotation declaration with 'source' attach point(s) should be a 'const' declaration", 17, 1);
        BAssertUtil.validateError(compileResult, 1, "missing source keyword", 17, 30);
        BAssertUtil.validateError(compileResult, 2, "missing source keyword", 18, 28);
        BAssertUtil.validateError(compileResult, 3,
                "annotation declaration with 'source' attach point(s) should be a 'const' declaration", 19, 1);
        BAssertUtil.validateError(compileResult, 4, "missing source keyword", 19, 22);
        BAssertUtil.validateError(compileResult, 5, "missing source keyword", 20, 45);
        BAssertUtil.validateError(compileResult, 6, "missing source keyword", 21, 37);
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
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        BAssertUtil.validateError(compileResult, 0, "expression is not a constant expression", 35, 14);
    }
}
