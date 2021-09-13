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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for service declaration symbols.
 *
 * @since 2.0.0
 */
public class ServiceSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/service_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testServiceDecl() {
        final List<String> expMethodNames = List.of("get one", "two", "three", "four");
        final List<String> expFieldNames = List.of("name", "id", "value");

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(20, 0));
        assertTrue(symbol.isPresent());

        assertEquals(symbol.get().kind(), SymbolKind.SERVICE_DECLARATION);
        ServiceDeclarationSymbol serviceDeclSymbol = (ServiceDeclarationSymbol) symbol.get();

        // check documentation (metadata)
        Optional<Documentation> docs = serviceDeclSymbol.documentation();
        assertTrue(docs.isPresent());
        assertTrue(docs.get().description().isPresent());
        assertEquals(docs.get().description().get(), "A listener");

        // check annotation (metadata)
        List<AnnotationSymbol> annots = serviceDeclSymbol.annotations();
        assertEquals(annots.size(), 1);
        assertEquals(annots.get(0).getName().get(), "a1");

        // check qualifier
        List<Qualifier> qualifiers = serviceDeclSymbol.qualifiers();
        // TODO: Fix https://github.com/ballerina-platform/ballerina-lang/issues/32661
//        assertEquals(qualifiers.size(), 1);
//        assertEquals(qualifiers.get(0), Qualifier.ISOLATED);

        // check methods
        Map<String, ? extends MethodSymbol> methods = serviceDeclSymbol.methods();
        assertEquals(methods.size(), 4);
        assertList(methods, expMethodNames);

        // check fields
        Map<String, ClassFieldSymbol> fields = serviceDeclSymbol.fieldDescriptors();
        assertEquals(fields.size(), 3);
        assertList(fields, expFieldNames);
    }

    @Test(dataProvider = "ServiceDeclFields")
    public void testServiceDeclFields(int line, int col, String expName, TypeDescKind typeKind, String expDocs,
                                      String expAnnot, List<Qualifier> expQuals) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.CLASS_FIELD);

        ObjectFieldSymbol field = (ObjectFieldSymbol) symbol.get();

        // check name
        assertEquals(field.getName().get(), expName);

        // check docs (metadata)
        Optional<Documentation> fieldDocs = field.documentation();
        assertTrue(fieldDocs.isPresent());
        assertTrue(fieldDocs.get().description().isPresent());
        assertEquals(fieldDocs.get().description().get(), expDocs);

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = field.annotations();
        assertEquals(fieldAnnots.size(), 1);
        assertEquals(fieldAnnots.get(0).getName().get(), expAnnot);

        // check qualifiers
        if (expQuals.size() > 0) {
            List<Qualifier> qualifiers = field.qualifiers();
            expQuals.forEach(qualifiers::contains);
        } else {
            assertTrue(field.qualifiers().isEmpty());
        }

        // check type
        assertEquals(field.typeDescriptor().typeKind(), typeKind);
    }

    @DataProvider(name = "ServiceDeclFields")
    public Object[][] getServiceDeclFields() {
        return new Object[][]{
                {24, 17, "name", TypeDescKind.STRING, "Name", "a3", List.of(Qualifier.FINAL)},
                {28, 14, "id", TypeDescKind.INT, "ID", "a3", List.of(Qualifier.FINAL)},
                {32, 16, "value", TypeDescKind.FLOAT, "Value", "a3", List.of(Qualifier.FINAL)}
        };
    }

    @Test(dataProvider = "ServiceDeclMethods")
    public void getServiceDeclMethods(int line, int col, String expName, SymbolKind kind, String expDocs,
                                      String expAnnot, List<Qualifier> expQuals) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);

        MethodSymbol method = (MethodSymbol) symbol.get();

        // check name
        assertEquals(method.getName().get(), expName);

        // check docs (metadata)
        Optional<Documentation> methodDocs = method.documentation();
        assertTrue(methodDocs.isPresent());
        assertTrue(methodDocs.get().description().isPresent());
        assertEquals(methodDocs.get().description().get(), expDocs);

        // check annotations (metadata)
        List<AnnotationSymbol> methodAnnots = method.annotations();
        assertEquals(methodAnnots.size(), 1);
        assertEquals(methodAnnots.get(0).getName().get(), expAnnot);

        // check qualifiers
        if (expQuals.size() > 0) {
            List<Qualifier> qualifiers = method.qualifiers();
            expQuals.forEach(qualifiers::contains);
        } else {
            assertTrue(method.qualifiers().isEmpty());
        }
    }

    @DataProvider(name = "ServiceDeclMethods")
    public Object[][] getServiceDeclMethods() {
        return new Object[][]{
                {38, 22, "get", SymbolKind.RESOURCE_METHOD, "Get one", "a2",
                        List.of(Qualifier.RESOURCE)},
                {44, 29, "two", SymbolKind.METHOD, "Method two", "a2",
                        List.of(Qualifier.PUBLIC, Qualifier.ISOLATED)},
                {49, 43, "three", SymbolKind.METHOD, "Method three", "a2",
                        List.of(Qualifier.TRANSACTIONAL, Qualifier.PUBLIC)},
                {54, 20, "four", SymbolKind.METHOD, "Method four", "a2",
                        List.of(Qualifier.REMOTE)}
        };
    }
}
