/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.vardeclr;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test module variable declaration negative.
 *
 * @since 1.2.2
 */
public class ModuleVarDeclareNegative {

    @Test
    public void setup() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/module-var-declare-negative.bal");

        Assert.assertEquals(result.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(result, i++, "uninitialized variable 's'", 2, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'a'", 3, 1);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 16, 13);
        BAssertUtil.validateError(result, i, "variable 's' is not initialized", 17, 18);
    }
}
