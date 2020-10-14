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
package org.ballerinalang.test.object;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for service type definitions.
 *
 * @since 2.0
 */
public class ServiceObjectTest {

    @Test(description = "Test basic service object type")
    public void testBasicServiceObjectType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/service_object_type_test.bal");
        // TODO: This is a negative negative test, hence does not do any assertions (used for developing this, can be deleted after )
        BValue[] returns = BRunUtil.invoke(compileResult, "testSerivceObjectAssignability");
    }

    @Test(description = "Test basic service object type with a remote method")
    public void testBasicServiceTypeWithRemoteMethod() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/object/service_object_type_with_remote_method_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSerivceObjectAssignability");
    }


    @Test(description = "Test basic service object type with a simple stored resource")
    public void testBasicServiceTypeWithSimpleStoredResource() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/object/service_object_type_with_simple_stored_resource_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSerivceObjectAssignability");
    }

    @Test
    public void objectServiceObjectAssignmentNegativeTest() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_service_object_negative.bal");
        validateError(result, 0, "incompatible types: expected 'ServiceTwo', found 'Obj'", 34, 21);
        validateError(result, 1, "incompatible types: expected 'ServiceWithSingleMethod', found 'ServiceOne'", 36, 44);
        validateError(result, 2, "incompatible types: expected 'ObjWithSingleMethod', found 'ServiceWithRemoteMethod'",
                61, 29);
        validateError(result, 3, "resource fields are only allowed in service types", 66, 5);
        validateError(result, 4, "incompatible types: expected 'DualAccessorService', found 'SingleAccessorService'",
                83, 32);
        Assert.assertEquals(result.getErrorCount(), 5);
    }
}
