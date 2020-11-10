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
 */

package org.ballerinalang.langlib.test;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.util.BLangConstants.BOOLEAN_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.BOOLEAN_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * This class tests boolean lang module functionality.
 *
 * @since 1.2.0
 */
public class LangLibBooleanTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/booleanlib_test.bal");
    }

    @Test(dataProvider = "InputList")
    public void testFromString(String val, Object expectedVal) {
        BRunUtil.invoke(compileResult, "testFromString", new Object[]{StringUtils.fromString(val), expectedVal});
    }

    @DataProvider(name = "InputList")
    public Object[][] getStringValues() {
        return new Object[][]{
                {"true", true},
                {"false", false},
                {"True", true},
                {"False", false},
                {" true", getError(" true")},
                {"false ", getError("false ")},
                {"1", true},
                {"0", false},
                {"SomeString", getError("SomeString")},
                {"", getError("")},
                {"123", getError("123")},
                {"\ntrue", getError("\ntrue")},
        };
    }

    private BError getError(String value) {
        BString reason = getModulePrefixedReason(BOOLEAN_LANG_LIB, BOOLEAN_PARSING_ERROR_IDENTIFIER);
        BString msg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                           PredefinedTypes.TYPE_STRING, value,
                                                           PredefinedTypes.TYPE_BOOLEAN);
        return ErrorCreator.createError(reason, msg);
    }
}
