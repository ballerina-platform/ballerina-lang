/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test module level listener declaration.
 *
 * @since 2.0
 */
public class ModuleListenerDeclTest {

    @Test
    public void testModuleListenerDeclaration() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/module_listener_decl.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testImportedModuleListenersDeclarations() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/listener-project");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void testModuleLevelErrorVarDeclNegative() {
        CompileResult result = BCompileUtil.compile("test-src/statements/vardeclr/module_listener_decl_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++,
                "incompatible types: expected 'listener', found '(Listener|Listener2|error)'", 18, 15);
        BAssertUtil.validateError(result, i++,
                "incompatible types: expected 'listener', found '(Listener2|error)'", 20, 15);
        BAssertUtil.validateError(result, i++,
                "listener variable incompatible types: 'l4' is not a Listener object", 21, 1);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
