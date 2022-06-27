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
package org.ballerinalang.test.services;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

public class ClientResourceAccessReturnTest {
    @Test()
    public void testClientServiceAccessReturn() {
        CompileResult compileResult = BCompileUtil.
                compile("test-src/services/client_resource_return_negative.bal");
        int i = 0;
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 14, 81);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 18, 94);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 18, 94);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 23, 89);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 27, 89);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 31, 21);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 31, 21);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 37, 21);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 46, 17);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 51, 17);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 56, 17);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 61, 17);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 69, 81);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 74, 25);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 74, 25);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'client object { }' as return type", 78, 89);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 86, 83);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 90, 91);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 94, 91);
        validateError(compileResult, i++, "Client resource access does not allow " +
                "'function type' as return type", 99, 25);
    }
}
