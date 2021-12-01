/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Test cases for record type symbols.
 *
 * @since 2.0.0
 */
public class TypeDefSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/module_type_def_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }


    @Test(dataProvider = "TypeDefPosProvider")
    public void testTypeDef(int line, int offset, String name, TypeDescKind typeKind) {
        TypeDefinitionSymbol symbol = (TypeDefinitionSymbol) assertBasicsAndGetSymbol(model, srcFile,
                                                                                      line, offset, name,
                                                                                      SymbolKind.TYPE_DEFINITION);

        TypeSymbol typeSymbol = symbol.typeDescriptor();
        assertEquals(typeSymbol.typeKind(), typeKind);

        assertFalse(symbol.qualifiers().isEmpty());
        assertEquals(symbol.qualifiers().get(0), Qualifier.PUBLIC);

        assertFalse(symbol.annotations().isEmpty());
        assertEquals(symbol.annotations().get(0).getName().get(), "v1");
    }

    @DataProvider(name = "TypeDefPosProvider")
    public Object[][] getTypeDefPos() {
        return new Object[][]{
                // Simple
                {17, 16, "nilType", TypeDescKind.NIL},
                {19, 16, "boolType", TypeDescKind.BOOLEAN},
                {21, 16, "intType", TypeDescKind.INT},
                {23, 16, "floatType", TypeDescKind.FLOAT},
                {25, 16, "decimalType", TypeDescKind.DECIMAL},

                // Sequence
                {28, 16, "stringType", TypeDescKind.DECIMAL},
                {30, 16, "xmlType", TypeDescKind.XML},

                // Structural
                {33, 16, "arrType", TypeDescKind.ARRAY},
                {35, 16, "tupleType", TypeDescKind.TUPLE},
                {37, 16, "mapType", TypeDescKind.MAP},
                {39, 16, "recType", TypeDescKind.RECORD},
                {44, 16, "tableType", TypeDescKind.TABLE},
                {47, 16, "errorType", TypeDescKind.ERROR},

                // Behavioural
                {49, 16, "funcType", TypeDescKind.FUNCTION},
                {51, 16, "objType", TypeDescKind.OBJECT},
                {55, 16, "futureType", TypeDescKind.FUTURE},
                {57, 16, "typeDescType", TypeDescKind.TYPEDESC},
                {59, 16, "handleType", TypeDescKind.HANDLE},
                {61, 16, "streamtype", TypeDescKind.STREAM},

                // Other
                {64, 16, "typeRefType", TypeDescKind.TYPE_REFERENCE},
                {66, 16, "anyType", TypeDescKind.ANY},
                {68, 16, "neverType", TypeDescKind.NEVER},
                {70, 16, "readonlyType", TypeDescKind.READONLY},
                {72, 16, "distinctObjType", TypeDescKind.OBJECT},
                {76, 16, "unionType", TypeDescKind.UNION},
                {78, 16, "interType", TypeDescKind.STRING},
                {80, 16, "optionalType", TypeDescKind.UNION},
                {82, 16, "anyDataType", TypeDescKind.ANYDATA},
                {84, 16, "jsonType", TypeDescKind.JSON},
                {86, 16, "byteType", TypeDescKind.BYTE},
                {88, 16, "otherType", TypeDescKind.TYPE_REFERENCE},
        };
    }
}
