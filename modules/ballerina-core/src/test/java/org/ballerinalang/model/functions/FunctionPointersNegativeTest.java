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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for Function pointers and lambda.
 *
 * @since 0.90
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

    @Test(expectedExceptions = SemanticException.class)
    public void testAnyToFunctionPointer() {
        // TODO : Fix this. This is not supported in 0.90 release. issue #2944
        ProgramFile programFile = BTestUtils.getProgramFile("lang/functions/negative/fp2any.bal");
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test1");
    }

    @Test (expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*undefined function 'getFullName'.*")
    public void testFPInStruct() {
        BTestUtils.getProgramFile("lang/functions/negative/fp-struct-negative.bal");
    }

    @Test (expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*incorrect arguments for function pointer - 'getName'.*")
    public void testFPInStructIncorrectArg() {
        BTestUtils.getProgramFile("lang/functions/negative/fp-struct-incorrect-arg.bal");
    }



}
