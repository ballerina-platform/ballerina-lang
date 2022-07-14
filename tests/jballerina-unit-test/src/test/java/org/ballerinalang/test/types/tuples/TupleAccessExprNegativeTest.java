/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.types.tuples;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tuple access with indexes negative test.
 */
public class TupleAccessExprNegativeTest {

    @Test
    public void testTupleAccessExprNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/types/tuples/tuple_access_expr_negative.bal");
        int index = 0;
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                26, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                27, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 29, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 30, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                33, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                36, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 39, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 42, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                49, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                50, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                51, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                53, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 54, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 55, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                58, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                61, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                64, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                67, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 70, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 73, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                81, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                82, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                83, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                84, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                85, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 87, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 88, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                89, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 90, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 91, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                94, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                97, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                100, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                103, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                106, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 109, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 112, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                115, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 118, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 121, 13);
        assertEquals(result.getErrorCount(), index);
    }
}
