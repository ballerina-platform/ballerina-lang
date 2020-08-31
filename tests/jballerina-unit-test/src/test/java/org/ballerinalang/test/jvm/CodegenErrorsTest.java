/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Test cases to cover scenarios where backend-jvm code generates errors.
 *
 * @since 1.0
 */
@Test
public class CodegenErrorsTest {

    @Test
    public void testTooLargeMethod() {
        CompileResult result = BCompileUtil.compileOnly("test-src/jvm/too-large-method.bal");
        BAssertUtil.validateError(result, 0, "method is too large: 'getXML'", "too-large-method.bal", 17, 1);
    }

    @Test
    public void testTooLargeObjectMethod() {
        CompileResult result = BCompileUtil.compileOnly("test-src/jvm/too-large-object-field.bal");
        BAssertUtil.validateError(result, 0, "method is too large: '$init$'", "too-large-object-field.bal", 17, 1);
    }

    @Test
    public void testTooLargeObjectField() {
        CompileResult result = BCompileUtil.compileOnly("test-src/jvm/too-large-object-method.bal");
        BAssertUtil.validateError(result, 0, "method is too large: 'getXML'", "too-large-object-method.bal", 18, 5);
    }

    @Test
    public void testTooLargePackageVar() {
        CompileResult result = BCompileUtil.compileOnly("test-src/jvm/too-large-package-variable.bal");
        BAssertUtil.validateError(result, 0, "method is too large: '..<init>'", ".", 1, 1);
    }
}
