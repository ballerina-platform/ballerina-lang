/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.bala.constant;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for reading list constants.
 *
 *  @since 2201.9.0
 */
public class ListConstantInBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/constant/list-literal-constant.bal");
    }

    @Test(dataProvider = "listAccessTestDataProvider")
    public void testConstantListAccess(String testCase) {
        try {
            BRunUtil.invoke(compileResult, testCase);
        } catch (Exception e) {
            Assert.fail(testCase + " test case failed.");
        }
    }

    @DataProvider(name = "listAccessTestDataProvider")
    public Object[] listAccessTestDataProvider() {
        return new Object[]{
                "testSimpleArrayAccess",
                "testSimpleTupleAccess",
                "test2DTupleAccess",
                "test2DArrayAccess",
                "testFixedLengthArrayAccess",
                "testArrayWithRestAccess",
                "test2DUnionArrayAccess"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
