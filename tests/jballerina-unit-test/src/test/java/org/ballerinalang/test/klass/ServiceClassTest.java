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
package org.ballerinalang.test.klass;

import org.ballerinalang.nativeimpl.jvm.servicetests.ServiceValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for service classes.
 *
 * @since 2.0
 */
public class ServiceClassTest {

    @Test
    public void testBasicStructAsObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/klass/simple_service_class.bal");
        BRunUtil.invoke(compileResult, "testServiceObjectValue");
    }

    @Test
    public void testResourceMethodsDoesNotAffectAssignability() {
        CompileResult compileResult = BCompileUtil.compile("test-src/klass/resource-method-assignability-test.bal");
        BRunUtil.invoke(compileResult, "testServiceObjectValue");
    }

    @Test
    public void testResourcePathParamNegative() {
        CompileResult result = BCompileUtil.compile("test-src/klass/simple_service_class_neg_path_param.bal");
        int index = 0;
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'json'", 37, 31);
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'anydata'", 37, 40);
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as rest path param, " +
                        "found 'anydata'", 37, 64);
        Assert.assertEquals(index, 3);
    }

    @Test
    public void testServiceObjectAndUsingServiceObjectAsATypeInclusionNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/klass/service_type_resource_method_decl_neg.bal");
        int index = 0;
        validateError(result, index++, "resource method declarations are not allowed in an object type definition",
                19, 5);
        validateError(result, index++, "resource method declarations are not allowed in an object type definition",
                20, 5);
        validateError(result, index++, "no implementation found for the method 'onMesage' of class 'SClass'", 23, 1);
        validateError(result, index++, "incompatible types: expected 'Foo', found 'Baz'", 88, 13);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testResourceFunctionWithInvalidPathParam() {
        CompileResult result =
                BCompileUtil.compile("test-src/klass/resource_function_with_invalid_path_param_type_negative.bal");
        int index = 0;
        validateError(result, index++, "only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                        "are supported as path params, found 'other'",
                22, 28);
        validateError(result, index++, "undefined module 'module1'",
                22, 29);
        validateError(result, index++, "unknown type 'RequestMessage'", 22, 29);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void reset() {
        ServiceValue.reset();
    }
}
