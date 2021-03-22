/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.incompletesource;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for incomplete source files.
 *
 * @since 2.0.0
 */
public class IncompleteSourceTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/incomplete-sources/undefined_type_in_fields.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testSymbolAtCursor() {
        ClassFieldSymbol field = (ClassFieldSymbol) model.symbol(srcFile, from(17, 16)).get();
        assertEquals(field.getName().get(), "c");
        assertEquals(field.typeDescriptor().typeKind(), TypeDescKind.COMPILATION_ERROR);
    }

    @Test
    public void testVisibleSymbols() {
        Map<String, Symbol> symbolsInFile =
                SemanticAPITestUtils.getSymbolsInFile(model, srcFile, 18, 5, new BallerinaModuleID(PackageID.DEFAULT));

        assertEquals(symbolsInFile.size(), 2);

        ClassFieldSymbol field = (ClassFieldSymbol) symbolsInFile.get("c");
        assertEquals(field.getName().get(), "c");
        assertEquals(field.typeDescriptor().typeKind(), TypeDescKind.COMPILATION_ERROR);

        ClassSymbol clazz = (ClassSymbol) symbolsInFile.get("Foo");
        assertEquals(clazz.getName().get(), "Foo");
    }
}
