/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.balo.record;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * BALO test cases for records.
 *
 * @since 0.990.0
 */
public class RecordInBaloTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project/", "testorg", "records");
        result = BCompileUtil.compile("test-src/record/rest_in_balo.bal");
    }

    @Test
    public void testRestFieldTypeDefAfterRecordDef() {
        BValue[] returns = BRunUtil.invoke(result, "testORRestFieldInOR");
        assertEquals(returns[0].stringValue(), "{name:\"Open Foo\", ob:{x:1.0}}");

        returns = BRunUtil.invoke(result, "testORRestFieldInCR");
        assertEquals(returns[0].stringValue(), "{name:\"Closed Foo\", ob:{x:2.0}}");

        returns = BRunUtil.invoke(result, "testCRRestFieldInOR");
        assertEquals(returns[0].stringValue(), "{name:\"Open Foo\", cb:{x:3.0}}");

        returns = BRunUtil.invoke(result, "testCRRestFieldInCR");
        assertEquals(returns[0].stringValue(), "{name:\"Closed Foo\", cb:{x:4.0}}");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project/", "testorg", "records");
    }
}
