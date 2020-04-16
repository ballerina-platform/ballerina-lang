/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.functions;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Arrow Expressions used in Iterable Functions.
 *
 * @since 1.2.1
 */
public class UndefinedFunctionsTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/undefined-functions.bal");
    }

    @Test
    public void testUndefinedFunctions() {
        Assert.assertEquals(result.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined function 'index' in type 'string'", 3, 17);
        BAssertUtil.validateError(result, i++, "undefined function 'add' in type 'string'", 4, 16);
        BAssertUtil.validateError(result, i++, "undefined function 'length' in type 'string?'", 17, 30);
        BAssertUtil.validateError(result, i++, "undefined function 'delete' in type 'map<string>'", 26, 13);
    }
}
