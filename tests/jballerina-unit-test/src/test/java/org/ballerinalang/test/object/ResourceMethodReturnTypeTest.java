/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.object;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for return types in resource functions.
 */
public class ResourceMethodReturnTypeTest {
    @Test()
    public void testResourceMethodReturnType() {
        CompileResult compileResult = BCompileUtil.
                compile("test-src/object/resource_method_return_type_negative.bal");
        int i = 0;
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 30, 58);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 34, 71);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 34, 71);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 39, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 43, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 48, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 48, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 53, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 61, 60);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 66, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 71, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 76, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 84, 58);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 89, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 89, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 93, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 101, 60);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 105, 68);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 109, 68);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 114, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 118, 80);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 118, 80);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
