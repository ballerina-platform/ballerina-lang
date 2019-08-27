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
        Assert.assertEquals(compileResult.getErrorCount(), 57);

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
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 6, 27);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 20, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 21, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 27, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                34, 21);
        BAssertUtil.validateError(compileResult, index++, "invalid literal for type 'string'", 36, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 41, 7);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'XYZ'",
                58, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                67, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '(E|F)', found '(D|E)'",
                85, 11);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[UVW, UVW]'", 92, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[IJK, IJK]'", 97, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'SSS'", 97, 18);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[LMN, OPQ, LMN]'", 102, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 102, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[OPQ, LMN, OPQ]'", 104, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", 104, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                109, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, EGI, ACE]'",
                109, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                109, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                111, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, ACE, BDF]'",
                111, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                111, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'DFH'", 111, 26);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, ACE, BDF, CEG]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, ACE, BDF, CEG]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, BDF, CEG]'",
                113, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, ACE, BDF, CEG, EGI]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, BDF, CEG, EGI]'",
                117, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'PQ'", 122, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'J'", 127, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'S'", 131, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'T'", 131, 12);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'U'", 131, 14);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                148, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                157, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '40', found 'int'", 168, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '20', found 'int'", 177, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '240', found 'int'",
                188, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0f', found 'float'",
                199, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '2.0f', found 'float'",
                208, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0d', found 'float'",
                219, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina is awesome', found" +
                " 'string'", 230, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina rocks', found " +
                "'string'", 239, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '()'",
                245, 24);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 249, 33);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", 250, 33);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", 262, 5);
        BAssertUtil.validateError(compileResult, index++, "constant cannot be defined with type 'Foo', expected a " +
                "simple basic types or a map of a simple basic type", 272, 7);
        BAssertUtil.validateError(compileResult, index, "constant cannot be defined with type 'json', expected a " +
                "simple basic types or a map of a simple basic type", 274, 7);
    }
}
