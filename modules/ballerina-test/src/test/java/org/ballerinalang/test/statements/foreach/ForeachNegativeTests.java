/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.foreach;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Negative test cases for Foreach Statement.
 *
 * @since 0.96.0
 */
public class ForeachNegativeTests {

    @Test
    public void testSemanticErrors() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/foreach/foreach-negative.bal");
        BAssertUtil.validateError(compile, 0, "too many variables are defined for iterable type 'string[]'", 3, 24);
        BAssertUtil.validateError(compile, 1, "redeclared symbol 'i'", 12, 13);
        BAssertUtil.validateError(compile, 2, "redeclared symbol 's'", 12, 16);
        BAssertUtil.validateError(compile, 3, "undefined symbol 'i'", 22, 13);
        BAssertUtil.validateError(compile, 4, "incompatible types: 'string' is not an iterable collection", 27, 18);
        BAssertUtil.validateError(compile, 5, "invalid assignment in variable 'p.id'", 40, 13);
        BAssertUtil.validateError(compile, 6, "too many variables are defined for iterable type 'string[]'", 48, 24);
        BAssertUtil.validateError(compile, 7, "incompatible types: expected 'int', found 'string'", 54, 18);
        BAssertUtil.validateError(compile, 8, "incompatible types: expected 'int', found 'string'", 54, 23);
        BAssertUtil.validateError(compile, 9,
                "incompatible types: 'json' cannot be convert to 'json[]', use cast expression", 61, 15);
    }
}
