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
        Assert.assertEquals(compileResult.getErrorCount(), 59);

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
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 24, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 25, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 31, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                38, 21);
        BAssertUtil.validateError(compileResult, index++, "invalid literal for type 'string'", 40, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 45, 7);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'XYZ'",
                62, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                71, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '(E|F)', found '(D|E)'",
                89, 11);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[UVW, UVW]'", 96, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[IJK, IJK]'", 101, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'SSS'", 101, 18);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[LMN, OPQ, LMN]'", 106, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 106, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[OPQ, LMN, OPQ]'", 108, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 108, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, EGI, ACE]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, ACE, BDF]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                115, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'DFH'", 115, 26);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, ACE, BDF, CEG]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, ACE, BDF, CEG]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, BDF, CEG]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                121, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                121, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, ACE, BDF, CEG, EGI]'",
                121, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, BDF, CEG, EGI]'",
                121, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'PQ'", 126, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'J'", 131, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'S'", 135, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'T'", 135, 12);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'U'", 135, 14);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                152, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                161, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '40', found 'int'", 172, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '20', found 'int'", 181, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '240', found 'int'",
                192, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0f', found 'float'",
                203, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '2.0f', found 'float'",
                212, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0d', found 'float'",
                223, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina is awesome', found" +
                " 'string'", 234, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina rocks', found " +
                "'string'", 243, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '()'",
                249, 24);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 253, 33);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 254, 33);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 266, 5);
        BAssertUtil.validateError(compileResult, index++, "constant cannot be defined with type 'Foo', expected a " +
                "simple basic types or a map of a simple basic type", 276, 7);
        BAssertUtil.validateError(compileResult, index, "constant cannot be defined with type 'json', expected a " +
                "simple basic types or a map of a simple basic type", 278, 7);
    }
}
