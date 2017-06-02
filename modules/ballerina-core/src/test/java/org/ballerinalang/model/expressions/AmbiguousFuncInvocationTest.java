/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.expressions;


import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Local ambiguous function invocation with implicit cast test.
 *
 * @since 0.8.3
 */
public class AmbiguousFuncInvocationTest {

    @BeforeClass
    public void setup() {

    }

    @Test(description = "Test invoking function without any match",
          expectedExceptions = {SemanticException.class },
          expectedExceptionsMessageRegExp = "no-matching-func.bal:3: undefined function 'ambiguousFuncTest'")
    public void testNoMatchingFunc() {
        BTestUtils.getProgramFile("lang/expressions/no-matching-func.bal");
    }
}
