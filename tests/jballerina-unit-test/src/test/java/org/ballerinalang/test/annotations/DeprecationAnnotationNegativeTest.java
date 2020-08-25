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
 * Negative tests for deprecation annotation.
 *
 * @since 1.2.0
 */
public class DeprecationAnnotationNegativeTest {

    @Test(description = "Negative tests for deprecation annotation", groups = { "disableOnOldParser" })
    public void testDeprecationAnnotation() {

        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 23);
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 4, 1);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 8, 1);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 18, 1);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 27, 5);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 36, 1);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 50, 60);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 59, 15);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 59, 41);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 59, 60);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 71, 5);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here", 85
                , 1);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here",
                104, 1);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here",
                121, 1);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 145, 43);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 153, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 161, 5);
        BAssertUtil.validateError(compileResult, i++, "constructs annotated as '@deprecated' must have 'Deprecated' " +
                "documentation", 165, 5);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here",
                173, 1);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here",
                184, 1);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here",
                191, 1);
        BAssertUtil.validateError(compileResult, i++, "'Deprecated parameters' documentation is not allowed here", 196,
                1);
        BAssertUtil.validateError(compileResult, i++, "invalid documentation: 'Deprecated' documentation is only " +
                "allowed on constructs annotated as '@deprecated'", 225, 1);
        BAssertUtil.validateError(compileResult, i, "'Deprecated parameters' documentation is not allowed here", 226,
                1);
    }
}
