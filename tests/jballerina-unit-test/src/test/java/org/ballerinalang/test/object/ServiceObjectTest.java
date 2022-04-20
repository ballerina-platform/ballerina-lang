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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for service type definitions.
 *
 * @since 2.0
 */
public class ServiceObjectTest {

    @Test
    public void objectServiceObjectAssignmentNegativeTest() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_service_object_negative.bal");
        validateError(result, 0, "incompatible types: expected 'ServiceTwo', found 'Obj'", 34, 21);
        validateError(result, 1, "incompatible types: expected 'ServiceWithSingleMethod', found 'ServiceOne'", 36, 44);
        validateError(result, 2, "incompatible types: expected 'ObjWithSingleMethod', found 'ServiceWithRemoteMethod'",
                61, 29);
        Assert.assertEquals(result.getErrorCount(), 3);
    }
}
