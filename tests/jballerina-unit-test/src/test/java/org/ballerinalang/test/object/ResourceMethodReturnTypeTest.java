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
                "'client object' type: 'object { }'", 31, 58);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(int|object { }|function (int,string) returns ((int|string))|string|record" +
                " {| int a; anydata...; |})'", 35, 71);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(int|object { }|function (int,string) returns ((int|string))|string|record " +
                "{| int a; anydata...; |})'", 35, 71);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(object { } & readonly)'", 40, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: 'object { }?'", 44, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(int|object { }|function (int,string) returns ((int|string))|xml)?'",
                49, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(int|object { }|function (int,string) returns ((int|string))|xml)?'",
                49, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(object { }? & readonly)'", 54, 21);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'function (int,string) returns ((int|string))'",
                62, 60);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'function (int,string) returns ((int|string))'",
                67, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'function (int,string) returns ((int|string))?'",
                72, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(function (int,string) returns ((int|string))? & readonly)'",
                77, 17);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: 'CustomType1'", 85, 58);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(int|CustomType1|CustomType2)'", 90, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(int|CustomType1|CustomType2)'", 90, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(CustomType1 & readonly)'", 94, 66);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'CustomType2'", 102, 60);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(CustomType2 & readonly)'", 106, 68);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'CustomType2?'", 110, 68);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(CustomType2? & readonly)'", 115, 25);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: '(CustomType1|CustomType2)'", 119, 80);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: '(CustomType1|CustomType2)'", 119, 80);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'client object' type: 'CustomType3'", 123, 81);
        validateError(compileResult, i++, "return type of the resource function does not allow " +
                "'function' type: 'CustomType3'", 123, 81);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
