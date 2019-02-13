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
import org.testng.Assert;
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
        Assert.assertEquals(compile.getErrorCount(), 14);
        int index = 0;
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 4, 17);
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 13, 17);
        BAssertUtil.validateError(compile, index++, "redeclared symbol 'i'", 13, 18);
        BAssertUtil.validateError(compile, index++, "redeclared symbol 's'", 13, 21);
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 20, 17);
        BAssertUtil.validateError(compile, index++, "undefined symbol 'i'", 23, 16);
        BAssertUtil.validateError(compile, index++, "incompatible types: 'int' is not an iterable collection", 28,
                22);
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 41, 17);
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 49, 17);
        BAssertUtil.validateError(compile, index++, "invalid tuple variable; expecting a tuple type but found " +
                "'json' in type definition", 58, 17);
        BAssertUtil.validateError(compile, index++, "unreachable code", 71, 9);
        BAssertUtil.validateError(compile, index++, "unreachable code", 76, 9);
        BAssertUtil.validateError(compile, index++, "continue cannot be used outside of a loop", 78, 5);
        BAssertUtil.validateError(compile, index, "invalid tuple variable; expecting a tuple type but found " +
                "'string' in type definition", 84, 17);
    }
}
