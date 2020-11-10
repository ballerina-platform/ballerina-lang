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

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid field access: field 'name' not found in record type " +
                    "'Teacher'.*")
    public void testMapAnyDataClosedRecordAssignmentNegative() {

        BRunUtil.invoke(negativeRunTimeResult, "testMapAnyDataClosedRecordAssignmentNegative");
    }
}
