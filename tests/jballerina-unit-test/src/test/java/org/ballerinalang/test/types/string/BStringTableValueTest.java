/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.string;

import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test BString support in Object.
 */

public class BStringTableValueTest extends BStringTestCommons {

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/bstring-table-test.bal");
    }

    @Test
    public void testTableGeneration() {
        testAndAssert("testTableGeneration", 116);
    }

    @Test
    public void testTableWithArrayGeneration() {
        testAndAssert("testTableWithArrayGeneration", 55);
    }
}
