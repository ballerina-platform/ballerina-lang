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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class SimpleConstantNegativeTest {

    @Test()
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/" +
                "simple-literal-constant-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 61);

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
        BAssertUtil.validateError(compileResult, index++, "underscore is not allowed here",
                9, 14);
        BAssertUtil.validateError(compileResult, index++, "underscore is not allowed here",
                10, 7);
        BAssertUtil.validateError(compileResult, index++, "constant cannot be defined with type 'invalidType'," +
                " expected a simple basic types or a map of a simple basic type", 12, 7);
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
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 47, 7);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'XYZ'",
                64, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                73, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '(E|F)', found '(D|E)'",
                91, 11);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[UVW, UVW]'", 98, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[IJK, IJK]'", 103, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'SSS'", 103, 18);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[LMN, OPQ, LMN]'", 108, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 108, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[OPQ, LMN, OPQ]'", 110, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 110, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, EGI, ACE]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, ACE, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'DFH'", 117, 26);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, ACE, BDF, CEG]'",
                119, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, ACE, BDF, CEG]'",
                119, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, BDF, CEG]'",
                119, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, ACE, BDF, CEG, EGI]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, BDF, CEG, EGI]'",
                123, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'PQ'", 128, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'J'", 133, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'S'", 137, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'T'", 137, 12);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'U'", 137, 14);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                154, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                163, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '40', found 'int'", 174, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '20', found 'int'", 183, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '240', found 'int'",
                194, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0f', found 'float'",
                205, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '2.0f', found 'float'",
                214, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0d', found 'float'",
                225, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina is awesome', found" +
                " 'string'", 236, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina rocks', found " +
                "'string'", 245, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '()'",
                251, 24);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 255, 33);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 256, 33);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 268, 5);
        BAssertUtil.validateError(compileResult, index++, "constant cannot be defined with type 'Foo', expected a " +
                "simple basic types or a map of a simple basic type", 278, 7);
        BAssertUtil.validateError(compileResult, index, "constant cannot be defined with type 'json', expected a " +
                "simple basic types or a map of a simple basic type", 280, 7);
    }
}
