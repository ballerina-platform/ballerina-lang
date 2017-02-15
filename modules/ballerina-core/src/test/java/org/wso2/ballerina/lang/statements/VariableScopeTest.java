/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class VariableScopeTest {

    @Test(expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "variable-if-scope.bal:12: undefined symbol 'k'")
    public void testIfScope() {
        ParserUtils.parseBalFile("lang/statements/variable-if-scope.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "variable-else-scope.bal:9: undefined symbol 'b'")
    public void testElseScope() {
        ParserUtils.parseBalFile("lang/statements/variable-else-scope.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "variable-while-scope.bal:7: undefined symbol 'b'")
    public void testWhileScope() {
        ParserUtils.parseBalFile("lang/statements/variable-while-scope.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "variable-resource-scope.bal:10: undefined symbol 'b'")
    public void testResourceScope() {
        ParserUtils.parseBalFile("lang/statements/variable-resource-scope.bal");
    }

    @Test
    public void testScopeValue() {

        BallerinaFile balFile = ParserUtils.parseBalFile("lang/statements/variable-scope-value.bal");
        scopeValue(balFile, "scopeIfValue", 5, 10, 20, 6);
        scopeValue(balFile, "scopeIfValue", 13, 8, 7, 7);
        scopeValue(balFile, "scopeIfValue", 25, 30, 8, 100000);

        scopeValue(balFile, "scopeWhileScope", 5, 10, 20, 5);
        scopeValue(balFile, "scopeWhileScope", 13, 8, 7, 105);
        scopeValue(balFile, "scopeWhileScope", 40, 30, 8, 205);
        scopeValue(balFile, "scopeWhileScope", 40, 30, 50, 305);
    }

    private void scopeValue(BallerinaFile balFile, String functionName, int a, int b, int c, int expected) {
        BValue[] args = { new BInteger(a), new BInteger(b), new BInteger(c) };
        BValue[] returns = Functions.invoke(balFile, functionName, args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

}
