/*
 * Copyright (c) 2018, WSO2 Inc. (\"http\"://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    \"http\"://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.record;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * BALA test cases for records.
 *
 * @since 0.990.0
 */
public class RecordInBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/record/rest_in_bala.bal");
    }

    @Test
    public void testRestFieldTypeDefAfterRecordDef() {
        Object returns = BRunUtil.invoke(result, "testORRestFieldInOR");
        assertEquals(returns.toString(), "{\"name\":\"Open Foo\",\"ob\":{\"x\":1.0}}");

        returns = BRunUtil.invoke(result, "testORRestFieldInCR");
        assertEquals(returns.toString(), "{\"name\":\"Closed Foo\",\"ob\":{\"x\":2.0}}");

        returns = BRunUtil.invoke(result, "testCRRestFieldInOR");
        assertEquals(returns.toString(), "{\"name\":\"Open Foo\",\"cb\":{\"x\":3.0}}");

        returns = BRunUtil.invoke(result, "testCRRestFieldInCR");
        assertEquals(returns.toString(), "{\"name\":\"Closed Foo\",\"cb\":{\"x\":4.0}}");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
