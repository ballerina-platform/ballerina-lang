/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Local ambiguous function invocation with package name.
 *
 * @since 0.86
 */
public class AmbiguousFuncInvocationWithPackageTest {

    @BeforeClass
    public void setup() {

    }


    @Test(description = "Test invoking ambiguous functions with package name",
          expectedExceptions = {SemanticException.class },
          expectedExceptionsMessageRegExp = "lang/expressions/pkg/main/ambiguous-func-invocation-with-package.bal:7:" +
                                            " function reference 'ambiguousFunc' is ambiguous, functions " +
                                            "'lang.expressions.pkg.func:ambiguousFunc\\((float|any)\\)' and " +
                                            "'lang.expressions.pkg.func:ambiguousFunc\\((float|any)\\)' matches")
    public void testFuncInvocationWithAmbiguousFunction() {
        BTestUtils.parseBalFile("lang/expressions/pkg/main/");
    }
}
