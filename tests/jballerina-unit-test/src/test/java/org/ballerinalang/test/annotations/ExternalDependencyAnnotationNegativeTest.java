/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 * Negative tests for ExternalDependency annotation.
 *
 * @since 2201.9.0
 */
public class ExternalDependencyAnnotationNegativeTest {

    @Test(description = "Negative tests for ExternalDependency annotation")
    public void testExternalDependencyAnnotation() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/annotations/external_dependency_annotation_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++,
                "annotation 'ballerina/jballerina.java:0.0.0:ExternalDependency' is not allowed on var", 20, 1);
        BAssertUtil.validateError(compileResult, i++,
                "annotation 'ballerina/jballerina.java:0.0.0:ExternalDependency' is not allowed on const", 23, 1);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
