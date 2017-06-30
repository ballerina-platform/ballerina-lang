/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.core.nativeimpl.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test overloading nature of functions.
 */
public class FunctionOverloadTest {

    @BeforeClass
    public void setup() {
    }
    @Test(description = "Test function overloading which has different argument counts",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "function-overloading-diff-arg-count.bal:5: " +
                    "redeclared symbol 'testOverloading'")
    public void testFunctionOverloadingDifferentArgCount() {
        BTestUtils.getProgramFile("lang/functions/function-overloading-diff-arg-count.bal");
    }

    @Test(description = "Test functino overloading which has same argument count",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "function-overloading.bal:5: redeclared symbol 'testOverloading'")
    public void testFunctionOverloadingSameArgCountTest() {
        BTestUtils.getProgramFile("lang/functions/function-overloading.bal");
    }

    @Test(description = "Test if incorrect function overloading produces errors")
    public void testInvalidFunctionOverloading() {
        Exception ex = null;

        try {
            BTestUtils.parseBalFile("lang/functions/invalid-function-overloading.bal");
        } catch (Exception e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex != null);
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex.getClass() + ".");
            Assert.assertEquals(ex.getMessage(),
                    "invalid-function-overloading.bal:5: redeclared symbol 'testOverloading'");
        }
    }

}
