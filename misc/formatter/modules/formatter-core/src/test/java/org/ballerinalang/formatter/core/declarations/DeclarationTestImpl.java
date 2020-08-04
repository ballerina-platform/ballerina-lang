/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core.declarations;

import org.testng.annotations.Test;

/**
 * Test formatting for declarations.
 *
 * @since 2.0.0
 */
public class DeclarationTestImpl extends AbstractDeclarationTest {

    @Test(description = "Test the formatting of function definitions")
    public void testFunctionDefinitions() {
        testFile("source/function-definitions.bal", "expected/function-definitions.bal");
    }

    @Test(description = "Test the formatting of import declarations")
    public void testImportDeclarations() {
        testFile("source/import-declarations.bal", "expected/import-declarations.bal");
    }

    @Test(description = "Test the formatting of listener declarations")
    public void testListenerDeclarations() {
        testFile("source/listener-declarations.bal", "expected/listener-declarations.bal");
    }

    @Test(description = "Test the formatting of module class definitions")
    public void testModuleClassDefinitions() {
        testFile("source/module-class-definitions.bal", "expected/module-class-definitions.bal");
    }

    @Test(description = "Test the formatting of module type definitions")
    public void testModuleTypeDefinitions() {
        testFile("source/module-type-definitions.bal", "expected/module-type-definitions.bal");
    }

    @Test(description = "Test the formatting of module constant declarations")
    public void testModuleConstantDeclarations() {
        testFile("source/module-constant-declarations.bal", "expected/module-constant-declarations.bal");
    }

    @Test(description = "Test the formatting of module enumeration declarations")
    public void testModuleEnumDeclarations() {
        testFile("source/module-enum-declarations.bal", "expected/module-enum-declarations.bal");
    }

    @Test(description = "Test the formatting of module variable declarations")
    public void testModuleVarDeclarations() {
        testFile("source/module-variable-declarations.bal", "expected/module-variable-declarations.bal");
    }

    @Test(description = "Test the formatting of service declarations")
    public void testServiceDeclarations() {
        testFile("source/service-declarations.bal", "expected/service-declarations.bal");
    }
}
