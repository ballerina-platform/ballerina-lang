/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Tests calling of a function with error return inside a service.
 */
public class ServiceDeclTest {

    @Test()
    public void testServiceDecl() {
        CompileResult compileResult = BCompileUtil.compile("test-src/services/service_decl.bal");
        BRunUtil.invoke(compileResult, "testServiceDecl");
    }

    @Test()
    public void testServiceNameLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/services/service_decl_service_name_literal.bal");
        BRunUtil.invoke(compileResult, "testServiceName");
    }

    @Test
    public void testServiceAbsolutePathOrNameLiteralWithRespectToListenerAttach() {
        CompileResult result = BCompileUtil.compile("test-src/services/service_decl_negative.bal");
        int i = 0;

        validateError(result, i++, "service absolute path is not supported by listener", 36, 16);
        validateError(result, i++, "service absolute path is not supported by listener", 57, 14);
        validateError(result, i++, "service absolute path is not supported by listener", 61, 17);
        validateError(result, i++, "service absolute path is not supported by listener", 65, 21);
        validateError(result, i++, "service path literal is not supported by listener", 69, 27);
        validateError(result, i++, "service path literal is required by the listener", 92, 12);
        validateError(result, i++, "service absolute path is not supported by listener", 96, 14);
        validateError(result, i++, "service path literal is required by the listener", 96, 14);
        validateError(result, i++, "service absolute path is not supported by listener", 123, 14);
        validateError(result, i++, "service absolute path is required by the listener", 142, 27);
        validateError(result, i++, "service path literal is not supported by listener", 142, 27);
        validateError(result, i++, "service absolute path is required by the listener", 146, 12);
        Assert.assertEquals(i, result.getErrorCount());
    }
}
