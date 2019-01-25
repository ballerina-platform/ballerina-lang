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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class ConstantNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/constant-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 56);

        int index = 0;
        int offset = 1;
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'int'",
                offset, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found 'string'",
                offset += 1, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                offset += 1, 23);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found 'boolean'",
                offset += 1, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'decimal', found 'boolean'",
                offset += 1, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                offset += 1, 27);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", offset += 14, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", offset += 1, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot update constant value", offset += 6, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'",
                offset += 7, 21);
        BAssertUtil.validateError(compileResult, index++, "invalid literal for type 'string'", offset += 2, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", offset += 5, 1);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'def'", offset += 6, 5);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'string'",
                offset += 11, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'",
                offset += 9, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'E|F', found 'D|E'",
                offset += 18, 11);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[UVW, UVW]'", offset += 7, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'SSS'", offset += 5, 18);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[IJK, IJK]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", offset += 5, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[LMN, OPQ, LMN]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'RST'", offset += 2, 20);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[OPQ, LMN, OPQ]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'",
                offset += 5, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, EGI, ACE]'",
                offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'DFH'", offset += 2, 26);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, BDF]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, EGI, ACE, BDF]'",
                offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, BDF, CEG]'",
                offset += 2, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, EGI, ACE, BDF, CEG]'",
                offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[CEG, ACE, BDF, CEG]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, BDF, CEG, EGI]'",
                offset += 4, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[BDF, CEG, ACE, BDF]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[EGI, ACE, BDF, CEG, EGI]'",
                offset, 1);
        BAssertUtil.validateError(compileResult, index++, "cyclic type reference in '[ACE, BDF, CEG, ACE]'", offset, 1);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'PQ'", offset += 5, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'J'", offset += 5, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'S'", offset += 4, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'T'", offset, 12);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'U'", offset, 14);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                offset += 17, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                offset += 9, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '40', found 'int'",
                offset += 11, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '20', found 'int'", offset += 9,
                28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '240', found 'int'",
                offset += 11, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0', found 'float'",
                offset += 11, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '2.0', found 'float'",
                offset += 9, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0', found 'float'",
                offset += 11, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina is awesome', found" +
                " 'string'", offset += 11, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina rocks', found " +
                "'string'", offset += 9, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '()'",
                offset += 6, 24);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 4, 33);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 1, 33);
        BAssertUtil.validateError(compileResult, index, "cannot update constant value", offset += 12, 5);
    }
}
