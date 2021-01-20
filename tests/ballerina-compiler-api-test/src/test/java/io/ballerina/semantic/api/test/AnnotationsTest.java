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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static java.util.List.of;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for retrieving annotations from a symbol.
 *
 * @since 2.0.0
 */
public class AnnotationsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/annotations_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void test(int line, int col, SymbolKind kind, List<String> annots) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertEquals(symbol.get().kind(), kind);

        List<AnnotationSymbol> annotSymbols = ((Annotatable) symbol.get()).annotations();

        assertEquals(annotSymbols.size(), annots.size());
        for (int i = 0; i < annotSymbols.size(); i++) {
            assertEquals(annotSymbols.get(i).name(), annots.get(i));
        }
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {46, 6, CONSTANT, of("v1")},
//                {52, 12, TYPE_DEFINITION, of("v1")}, // TODO: Uncomment after fixing #27461
                {53, 15, RECORD_FIELD, of("v5")},
                {65, 6, CLASS, of("v1", "v2", "v2")},
                {66, 15, CLASS_FIELD, of("v5")},
                {71, 20, METHOD, of("v3")},
                {71, 69, VARIABLE, of("v4")},
                {81, 16, FUNCTION, of("v3")},
                {86, 11, ANNOTATION, of("v1")},
                {98, 18, CLASS_FIELD, of("v5")},
                {103, 22, METHOD, of("v3")},
//                {112, 11, WORKER, of("v1")} // TODO: Uncomment after fixing #27461
                {121, 5, ENUM, of("v1", "v5")},
                {125, 4, CONSTANT, of("v1")}
        };
    }
}
