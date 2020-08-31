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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative tests for validate annotation attachment expression.
 *
 * @since 1.2.1
 */
public class AnnotationExpressionCodeAnalysisNegative {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(
                "test-src/annotations/annot_attachment_expression_code_analysis_negative.bal");
    }

    @Test(description = "Validate the annotations attachment the expression")
    public void testAttachmentExpression() {
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "invalid key 'i': identifiers cannot be used as rest field " +
                "keys, expected a string literal or an expression", 26, 5);
        BAssertUtil.validateError(compileResult, i++, "'null' literal is only supported for 'json'", 40, 9);
        BAssertUtil.validateError(compileResult, i++, "invalid usage of record literal: duplicate key 'i' via spread " +
                "operator '...fl'", 64, 8);
        BAssertUtil.validateError(compileResult, i++, "invalid usage of record literal: duplicate key 's'", 65, 5);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

}
