/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.typereftype;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
/**
 * This class contains type reference type related test cases.
 */
public class TypeReferenceTests {

    @Test(description = "Test basics types")
    public void testNegative() {
        final CompileResult compileResult = BCompileUtil.compile("test-src/types/typereftype/typeref_negative.bal");
        int index = 0;

        validateError(compileResult, index++, "incompatible types: expected 'ImmutableIntArray', " +
                "found 'string'", 10, 32);
        validateError(compileResult, index++, "incompatible types: expected 'Foo', " +
                "found 'boolean'", 12, 15);
        validateError(compileResult, index++, "incompatible types: expected 'FunctionTypeOne', " +
                "found 'function (IntOrBoolean) returns (int)'", 14, 29);
        validateError(compileResult, index++, "incompatible types: expected 'FunctionTypeTwo', " +
                "found 'function (FooBar) returns (int)'", 18, 29);
        validateError(compileResult, index++, "incompatible types: expected 'Foo', found 'int'", 25, 22);
        validateError(compileResult, index++, "incompatible types: expected 'Foo', found 'int'", 28, 16);
        validateError(compileResult, index++, "incompatible types: expected 'string', " +
                "found 'ImmutableIntArray'", 32, 16);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}
