/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
import org.testng.annotations.Test;


/**
 * Contains tests for distinct errors in query.
 *
 * @since 2201.13.0
 */
public class QueryDistinctErrorTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-distinct-error.bal");
    }

    @Test
    public void testDistinctTypeMismatchError() {
        BRunUtil.invoke(result, "testDistinctTypeMismatchError");
    }

    @Test
    public void testQueryDistinctError() {
        BRunUtil.invoke(result, "testQueryDistinctError");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
