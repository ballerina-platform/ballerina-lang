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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Primitive add expression test.
 */
public class SubstractExprTest {

    /*
     * Negative tests
     */
    
    @Test(description = "Test substracting values of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "substract-incompatible-types.bal:5: invalid operation: " +
                    "incompatible types 'int' and 'string'")
    public void testAddIncompatibleTypes() {
        BTestUtils.parseBalFile("lang/expressions/substract-incompatible-types.bal");
    }
    
    @Test(description = "Test substracting values of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "substract-unsupported-types.bal:10: invalid operation: " +
                    "operator - not defined on 'json'")
    public void testSubtractUnsupportedTypes() {
        BTestUtils.parseBalFile("lang/expressions/substract-unsupported-types.bal");
    }
}
