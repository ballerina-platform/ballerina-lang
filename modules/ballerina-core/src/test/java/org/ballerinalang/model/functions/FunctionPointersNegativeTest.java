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
package org.ballerinalang.model.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Negative Test cases for Function pointers and lambda.
 */
public class FunctionPointersNegativeTest {


    @Test(expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'function \\(string,float\\) returns \\" +
                    "(boolean\\)' cannot be assigned to 'function \\(string,int\\) returns \\(boolean\\)'")
    public void testFunctionPointerAsVariable() {
        BTestUtils.getProgramFile("lang/functions/negative/fp-type-mismatch1.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'function \\(string,boolean\\) returns \\" +
                    "(boolean\\)' cannot be assigned to 'function \\(string,int\\) returns \\(boolean\\)'")
    public void testLambdaAsVariable() {
        BTestUtils.getProgramFile("lang/functions/negative/fp-type-mismatch2.bal");
    }

}
