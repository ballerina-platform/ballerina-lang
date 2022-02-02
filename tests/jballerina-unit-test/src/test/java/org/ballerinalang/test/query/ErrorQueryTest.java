/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query actions with records.
 *
 * @since 2.0.0
 */
public class ErrorQueryTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query_expr_with_errors.bal");
    }

    @Test
    public void queryAnErrorStream() {
        JvmRunUtil.invoke(result, "queryAnErrorStream");
    }

    @Test
    public void queryWithoutErrors() {
        JvmRunUtil.invoke(result, "queryWithoutErrors");
    }

    @Test
    public void queryWithAnError() {
        JvmRunUtil.invoke(result, "queryWithAnError");
    }

    @Test
    public void queryWithACheckFail() {
        JvmRunUtil.invoke(result, "queryWithACheckFailEncl");
    }

    @Test
    public void queryWithAPanic() {
        JvmRunUtil.invoke(result, "queryWithAPanicEncl");
    }

    @Test
    public void streamFromQueryWithoutErrors() {
        JvmRunUtil.invoke(result, "streamFromQueryWithoutErrors");
    }

    @Test
    public void streamFromQueryWithAnError() {
        JvmRunUtil.invoke(result, "streamFromQueryWithAnError");
    }

    @Test
    public void streamFromQueryWithACheckFail() {
        JvmRunUtil.invoke(result, "streamFromQueryWithACheckFail");
    }

    @Test
    public void streamFromQueryWithAPanic() {
        JvmRunUtil.invoke(result, "streamFromQueryWithAPanicEncl");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
