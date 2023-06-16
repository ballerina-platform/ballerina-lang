/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.map;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Neagative Test cases for constraining map.
 */
public class ConstrainedMapNegativeTest {

    private CompileResult negativeRunTimeResult;

    @BeforeClass
    public void setup() {

        negativeRunTimeResult = BCompileUtil.compile("test-src/types/map/map-closed-record-assignment" +
                "-negative.bal");
    }

    @Test(description = "Test closed record assignment to map which is constrained to anydata and access non-exist " +
            "field",
            expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid field access: field 'name' not found in record type " +
                    "'Teacher'.*")
    public void testMapAnyDataClosedRecordAssignmentNegative() {

        BRunUtil.invoke(negativeRunTimeResult, "testMapAnyDataClosedRecordAssignmentNegative");
    }

    @AfterClass
    public void tearDown() {
        negativeRunTimeResult = null;
    }
}
