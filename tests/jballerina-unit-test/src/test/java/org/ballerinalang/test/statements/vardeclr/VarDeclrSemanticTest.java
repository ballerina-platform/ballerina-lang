/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.vardeclr;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Class to test variable declaration semantics.
 */
public class VarDeclrSemanticTest {

    @BeforeClass
    public void setup() {
    }

    @Test
    public void varDeclrTest() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/var-def-negative.bal");
        assertEquals(result.getErrorCount(), 1);
        assertEquals(result.getWarnCount(), 0);
        validateError(result, 0, "incompatible types: expected 'int', found 'string'", 2, 13);
    }

    @Test
    public void testIncompleteListenerDecl() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/incomplete_listener_decl.bal");
        int indx = 0;
        validateError(result, indx++, "listener variable incompatible types: 'foo' is not a Listener object", 17, 1);
        validateError(result, indx++, "incompatible types: expected '(function|error)', found '()'", 19, 13);
        validateError(result, indx++, "missing equal token", 19, 13);
        validateError(result, indx++, "invalid record binding pattern with type 'other'", 19, 16);
        validateError(result, indx++, "missing semicolon token", 19, 16);
        validateError(result, indx++, "missing type descriptor", 19, 16);
        validateError(result, indx++, "missing semicolon token", 22, 1);
        validateError(result, indx++, "variable declaration having binding pattern must be initialized", 22, 1);
        assertEquals(result.getErrorCount(), indx);
    }

    @Test
    public void testIncompleteListenerDecl2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/incomplete_listener_decl_2.bal");
        int indx = 0;
        validateError(result, indx++, "listener variable incompatible types: '$missingNode$_0' is not a Listener " +
                        "object",
                      17, 1);
        validateError(result, indx++, "missing object keyword", 18, 1);
        validateError(result, indx++, "missing open brace token", 18, 1);
        validateError(result, indx++, "invalid token '{'", 19, 23);
        validateError(result, indx++, "missing semicolon token", 20, 1);
        validateError(result, indx++, "missing equal token", 22, 1);
        validateError(result, indx++, "missing expression", 22, 1);
        validateError(result, indx++, "missing identifier", 22, 1);
        validateError(result, indx++, "missing semicolon token", 22, 1);
        assertEquals(result.getErrorCount(), indx);
    }

    @Test
    public void testIncompleteVarDecl() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/incomplete_var_decl.bal");
        int indx = 0;
        validateError(result, indx++, "unknown type 'L'", 20, 26);
        validateError(result, indx++, "missing open parenthesis token", 21, 1);
        validateError(result, indx++, "missing comma token", 21, 17);
        validateError(result, indx++, "intervening whitespaces are not allowed in qualified identifier", 21, 30);
        validateError(result, indx++, "missing close parenthesis token", 22, 1);
        validateError(result, indx++, "missing close parenthesis token", 22, 1);
        validateError(result, indx++, "missing identifier", 22, 1);
        validateError(result, indx++, "missing open parenthesis token", 22, 1);
        validateError(result, indx++, "missing semicolon token", 22, 1);
        assertEquals(result.getErrorCount(), indx);
    }
}
