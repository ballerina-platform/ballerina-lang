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

public class ResourceMethodReturnTypeTest {
    @Test()
    public void testResourceMethodReturnType() {
        CompileResult compileResult = BCompileUtil.
                compile("test-src/object/client_resource_return_negative.bal");
        int i = 0;
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 30, 43);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 34, 94);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 34, 94);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 39, 89);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 43, 89);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 48, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 48, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 54, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 62, 43);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 67, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 72, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 77, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 85, 43);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 90, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 90, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' types", 94, 89);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 102, 43);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 106, 91);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 110, 91);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' types", 115, 25);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
