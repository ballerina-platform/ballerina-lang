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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * BALA test cases for records.
 *
 * @since 0.990.0
 */
public class RecordInBalaTest {

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
    }

    @Test
    public void testRestFieldTypeDefAfterRecordDef() {
        CompileResult result = BCompileUtil.compile("test-src/record/rest_in_bala.bal");
        BRunUtil.invoke(result, "testORRestFieldInOR");
        BRunUtil.invoke(result, "testORRestFieldInCR");
        BRunUtil.invoke(result, "testCRRestFieldInOR");
        BRunUtil.invoke(result, "testCRRestFieldInCR");
    }

    @Test
    public void testComplexCyclicRecordTypeResolution() {
        CompileResult typeResolution =
                BCompileUtil.compile("test-src/record/complex_record_resolution.bal");
        BRunUtil.invoke(typeResolution, "testCreatingAComplexRecordWithIncludingType");
        BRunUtil.invoke(typeResolution, "testCreatingComplexRecordWithIncludedType");
        BRunUtil.invoke(typeResolution, "testCreatingComplexRecWithIncTypeWithActualTypes");
        BRunUtil.invoke(typeResolution, "testCreatingComplexRecWithIncTypeFromBala");
        BRunUtil.invoke(typeResolution, "testCreatingComplexRecWithIncTypeFromBalaWithCM");
    }
}
