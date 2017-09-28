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

package org.ballerinalang.model.globalvar;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Global variable error scenarios.
 */
public class GlobalVarErrorTest {

    @BeforeClass
    public void setup() {

    }

    @Test(description = "Test struct fields with packages for child elements",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "global-var-error-function.bal:6: struct child fields cannot have " +
            "package identifiers: 'xyz:name'", enabled = false)
    public void testStructFieldWithChildPackagePaths() {
        BTestUtils.getProgramFile("lang/globalvar/global-var-error-function.bal");
    }

}
