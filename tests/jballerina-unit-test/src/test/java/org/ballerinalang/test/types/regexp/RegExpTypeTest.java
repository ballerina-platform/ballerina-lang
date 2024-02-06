/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.regexp;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test RegExp type.
 *
 * @since 2201.3.0
 */
public class RegExpTypeTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/regexp/regexp_type_test.bal");
    }

    @Test(dataProvider = "dataToTestRegExp")
    public void testRegExpType(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestRegExp() {
        return new Object[]{
                "testBasicRegExp",
                "testAssignabilityWithRegExp",
                "testSubtypingWithRegExp",
                "testRegExpWithVar",
                "testRegExpWithUserDefinedType",
                "testRegExpReadonlyLocalVars"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
