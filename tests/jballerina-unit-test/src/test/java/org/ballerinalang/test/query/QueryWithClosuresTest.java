/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.query;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expressions and query actions with closures.
 *
 * @since 2201.5.0
 */
public class QueryWithClosuresTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query_with_closures.bal");
    }

    @Test(dataProvider = "dataToTestQueryWithClosures")
    public void testQueryWithClosures(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @Test
    public void testQueriesWithinArrowFunctionsAndWithLet() {
        BRunUtil.invoke(result, "testQueriesWithinArrowFunctionsAndWithLet");
    }

    @DataProvider
    public Object[] dataToTestQueryWithClosures() {
        return new Object[]{
                "testClosuresInQueryInSelect",
                "testClosuresInQueryInterpolatedInXmlInSelect",
                "testClosureInQueryActionInDo",
                "testClosuresInQueryInLet",
                "testClosuresInNestedQueryInSelect",
                "testClosuresInNestedQueryInSelect2",
                "testClosureInQueryActionInDo2",
                "testClosureInQueryActionInDo3",
                "testClosureInQueryActionInDo4",
                "testClosureInQueryActionInDo5",
                "testClosureInQueryActionInDo6",
                "testClosureInQueryActionInDo7",
                "testNestedQueryAndClosureFieldInQuerySelect"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
