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

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/klass/simple_service_class.bal");
    }

    @Test
    public void testBasicStructAsObject() {
        BRunUtil.invoke(compileResult, "testServiceObjectValue");
    }

    @Test
    public void testResourcePathParamNegative() {
        CompileResult result = BCompileUtil.compile("test-src/klass/simple_service_class_neg_path_param.bal");
        int index = 0;
        validateError(result, index++,
                "only int, string, float, boolean types are supported as path params, found 'json'", 37, 31);
        validateError(result, index++,
                "only int, string, float, boolean types are supported as path params, found 'anydata'", 37, 40);
        validateError(result, index++,
                "only int, string, float, boolean types are supported as rest path param, found 'anydata'", 37, 64);
        Assert.assertEquals(index, 3);
    }

    @AfterClass
    public void reset() {
        ServiceValue.reset();
    }
}
