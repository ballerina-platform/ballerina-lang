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

    @Test(description = "Negative tests for deprecation annotation")
    public void testDeprecationAnnotation() {

        CompileResult compileResult = BCompileUtil.compile("test-src/annotations/deprecation_annotation_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 5);
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "deprecation documentation should be available, since " +
                "deprecation annotation is available", 4, 1);
        BAssertUtil.validateError(compileResult, i++, "deprecation annotation should be available, since deprecation " +
                "documentation is available", 10, 1);
        BAssertUtil.validateError(compileResult, i++, "deprecation documentation should be available, since " +
                "deprecation annotation is available", 18, 1);
        BAssertUtil.validateError(compileResult, i++, "deprecation annotation should be available, since deprecation " +
                "documentation is available", 23, 5);
        BAssertUtil.validateError(compileResult, i, "deprecation documentation should be available, since deprecation" +
                " annotation is available", 36, 1);
    }
}
