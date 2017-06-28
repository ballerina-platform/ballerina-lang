/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.constant;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant assignment test cases.
 */
public class ConstantAssignmentTest {

    @BeforeClass
    public void setup() {}

    @Test(description = "Test constant assignment float to int.",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "constant-assignment-float-to-int.bal:1: incompatible types: " +
                    "'float' cannot be assigned to 'int'")
    public void testConstantAssignmentFloatToInt() {
        BTestUtils.getProgramFile("lang/constant/constant-assignment-float-to-int.bal");
    }

    @Test(description = "Test accessing constant string to int.",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "constant-assignment-string-to-int.bal:1: incompatible types: " +
                    "'string' cannot be assigned to 'int'")
    public void testConstantAssignmentStringToInt() {
        BTestUtils.getProgramFile("lang/constant/constant-assignment-string-to-int.bal");
    }

    @Test(description = "Test accessing constant string to float.",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "constant-assignment-string-to-float.bal:1: incompatible types: " +
                    "'string' cannot be assigned to 'float'")
    public void testConstantAssignmentStringToFloat() {
        BTestUtils.getProgramFile("lang/constant/constant-assignment-string-to-float.bal");
    }

}
