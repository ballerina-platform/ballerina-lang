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
                28, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                29, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 31, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 32, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                35, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                38, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 41, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 44, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                51, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                52, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                53, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                55, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 56, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 57, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                60, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                63, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                66, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                69, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 72, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 75, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                87, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                88, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                89, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                90, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                91, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                92, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                93, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                94, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                95, 17);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 97, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 98, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                99, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 100, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 101, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                102, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                103, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                104, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                105, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                108, 16);
        validateError(result, index++, "incompatible types: expected 'int', found 'string'",
                111, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                114, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                117, 17);
        validateError(result, index++, "incompatible types: expected 'int', found 'boolean'",
                120, 13);
        validateError(result, index++, "incompatible types: expected 'string', found " +
                "'(int|string|boolean)'", 123, 16);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 126, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                129, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found " +
                "'(int|string|boolean)'", 132, 17);
        validateError(result, index++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 135, 13);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                138, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                141, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'",
                144, 16);
        validateError(result, index++, "incompatible types: expected 'boolean', found 'int'",
                147, 17);
        assertEquals(result.getErrorCount(), index);
    }
}
