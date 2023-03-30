/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class SimpleConstantNegativeTest {

    @Test()
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/types/constant/simple-literal-constant-negative.bal");

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'int'",
                1, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found 'string'",
                2, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                3, 23);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found 'boolean'",
                4, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'decimal', found 'boolean'",
                5, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                6, 27);
        BAssertUtil.validateError(compileResult, index++, "'_' is a keyword, and may not be used as an identifier",
                9, 14);
        BAssertUtil.validateError(compileResult, index++, "'_' is a keyword, and may not be used as an identifier",
                10, 7);
        BAssertUtil.validateError(compileResult, index++, "cannot declare a constant with type 'invalidType', " +
                        "expected a subtype of 'anydata' that is not 'never'",
                12, 7);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'invalidType'",
                12, 7);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 26, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 27, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 33, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                40, 21);
        BAssertUtil.validateError(compileResult, index++,
                                  "a type compatible with mapping constructor expressions not found in type 'string'",
                                  42, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 46, 7);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 47, 7);
        BAssertUtil.validateError(compileResult, index++, "symbol 'abc' is already initialized with 'abc'", 47, 7);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'ACTION', found '\"XYZ\"'",
                64, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                73, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'G', found 'H'",
                91, 11);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[UVW, UVW]'", 98, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[IJK, IJK]'", 103, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'SSS'", 103, 18);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[LMN, OPQ, LMN]'", 108, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 108, 20);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[OPQ, LMN, OPQ]'", 110, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 110, 20);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[ACE, BDF, CEG, EGI," +
                        " ACE]'", 115, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[BDF, CEG, EGI, ACE," +
                        " BDF]'", 117, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[CEG, ACE, BDF, CEG]'",
                119, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[CEG, EGI, ACE, BDF," +
                        " CEG]'", 119, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[CEG, EGI, BDF, CEG]'",
                119, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[EGI, ACE, BDF," +
                        " CEG, EGI]'", 123, 1);
        BAssertUtil.validateError(compileResult, index++, "invalid cyclic type reference in '[EGI, BDF, CEG, EGI]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'PQ'", 128, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'T'", 137, 12);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'U'", 137, 14);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'BooleanTypeWithType', " +
                "found 'boolean'", 154, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'BooleanTypeWithoutType', " +
                "found 'boolean'", 163, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'IntTypeWithType', " +
                "found 'int'", 174, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'IntTypeWithoutType', " +
                "found 'int'", 183, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'ByteTypeWithType', " +
                "found 'int'", 194, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'FloatTypeWithType', " +
                "found 'float'", 205, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'FloatTypeWithoutType', " +
                "found 'float'", 214, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'DecimalTypeWithType', " +
                "found 'float'", 225, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'StringTypeWithType', found" +
                " 'string'", 236, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'StringTypeWithoutType', " +
                "found 'string'", 245, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '()'",
                251, 24);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 255, 33);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 256, 33);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 268, 5);
        BAssertUtil.validateError(compileResult, index++, "constant declaration not yet supported for type 'Foo'",
                278, 7);
        BAssertUtil.validateError(compileResult, index++, "constant declaration not yet supported for type 'json'",
                280, 7);
        BAssertUtil.validateError(compileResult, index++, "constant declaration not yet supported for " +
                "type 'int:Signed32'", 282, 14);
        BAssertUtil.validateError(compileResult, index++, "constant declaration not yet supported for " +
                "type 'int:Unsigned16'", 284, 14);
        BAssertUtil.validateError(compileResult, index++, "constant declaration not yet supported for " +
                "type 'string:Char'", 286, 14);
        BAssertUtil.validateError(compileResult, index++, "cannot declare a constant with type 'Bar', " +
                        "expected a subtype of 'anydata' that is not 'never'",
                294, 7);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression",
                294, 17);
        BAssertUtil.validateError(compileResult, index++, "constant declarations are allowed only at module level",
                297, 5);
        BAssertUtil.validateError(compileResult, index++, "type is required for constants with expressions",
                301, 25);
        BAssertUtil.validateError(compileResult, index++, "cannot resolve constant 'UT_COUNT'", 302, 27);
        BAssertUtil.validateError(compileResult, index++, "self referenced constant 'CONST1'", 304, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc2'", 308, 7);
        BAssertUtil.validateError(compileResult, index++, "symbol 'abc2' is already initialized with '1'", 308, 7);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Byte', found 'int'", 312, 17);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ints', found 'float'",
                                  316, 17);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ints2', found 'int'",
                320, 15);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'NaNf', found 'float'",
                322, 22);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Infinityf', found 'float'",
                323, 27);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 332, 18);
        BAssertUtil.validateError(compileResult, index++, "invalid constant expression, reason '/ by zero'", 333, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 335, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 336, 18);
        BAssertUtil.validateError(compileResult, index++, "invalid constant expression, reason '/ by zero'", 338, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 339, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 341, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 342, 18);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 344, 19);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 345, 19);
        BAssertUtil.validateError(compileResult, index++, "invalid constant expression, reason 'Division by zero'",
                347, 23);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ANS7'", 349, 11);
        BAssertUtil.validateError(compileResult, index++, "symbol 'ANS7' is already initialized with 'null'", 349, 11);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 349, 18);
        BAssertUtil.validateError(compileResult, index++, "'9.999999999999999999999999999999999E+6146' " +
                "is out of range for 'decimal'", 351, 20);
        BAssertUtil.validateError(compileResult, index++, "'-9.999999999999999999999999999999999E+6146' " +
                "is out of range for 'decimal'", 352, 20);
        BAssertUtil.validateError(compileResult, index++, "'1.010000000000000000000000000000000E+6145' " +
                "is out of range for 'decimal'", 353, 20);
        BAssertUtil.validateError(compileResult, index++, "'-1.100000000000000000000000000000000E+6145'" +
                " is out of range for 'decimal'", 354, 20);
        BAssertUtil.validateError(compileResult, index++, "'5E+6413' is out of range for 'decimal'", 355, 20);
        BAssertUtil.validateError(compileResult, index++, "'int' range overflow", 357, 19);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}
