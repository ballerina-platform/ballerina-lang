/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.action;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for nested actons.
 *
 * @since 1.3.0
 */
public class TypeCastActionTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/action/typecast_action.bal");
    }

    @Test(dataProvider = "FunctionList")
    public void testTypecastActions(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: " +
                  "'int' cannot be cast to 'string'.*")
    public void testCastingToIncorrectType() {
        BRunUtil.invoke(result, "testCastingToIncorrectType");
    }

    @DataProvider(name = "FunctionList")
    public Object[][] testFunctions() {
        return new Object[][]{
                {"testTypecastWithRemoteMethodCalls"},
                {"testCastingRecords"},
                {"testCastingObjects"},
                {"testTypecastingActions"}
        };
    }
}
