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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for documentation() API.
 *
 * @since 2.0.0
 */
public class DocumentationTest {

    private SemanticModel model;
    private Document srcFile;
    private final Map<String, String> emptyMap = new HashMap<>();

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/documentation_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testAnnotationDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(27, 30));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is an annotation");
    }

    @Test
    public void testClassDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(30, 6));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is a class");

        ClassSymbol classSymbol = (ClassSymbol) symbol.get();
        classSymbol.fieldDescriptors().forEach(
                (name, field) -> assertDescriptionOnly(field.documentation().get(), "Field name"));
        classSymbol.methods().forEach(
                (name, method) -> assertDocumentation(method.documentation().get(), "Method getName", emptyMap,
                                                      "string"));

        assertDocumentation(classSymbol.initMethod().get().documentation().get(), "Method init",
                            Map.of("name", "Param name"), "error or nil");
    }

    @Test
    public void testConstantDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(47, 13));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is a constant");
    }

    @Test
    public void testEnumDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(50, 12));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is an enum");

        List<ConstantSymbol> members = ((EnumSymbol) symbol.get()).members();
        for (ConstantSymbol member : members) {
            assertDescriptionOnly(member.documentation().get(), "Enum member " + member.getName().get());
        }
    }

    @Test
    public void testTypeDefDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(19, 5));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDocumentation(documentation.get(), "This is a record", Map.of("foo", "Field foo", "bar", "Field bar"),
                            null);

        RecordTypeSymbol recordType = (RecordTypeSymbol) ((TypeDefinitionSymbol) symbol.get()).typeDescriptor();
        recordType.fieldDescriptors().forEach(
                (name, field) -> assertDescriptionOnly(field.documentation().get(), "Field " + name));
    }

    @Test
    public void testFunctionDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(63, 9));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDocumentation(documentation.get(), "This is a function", Map.of("x", "Param x", "y", "Param y"),
                            "The sum");
    }

    @Test
    public void testModuleVarDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(66, 7));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is a variable");
    }

    @Test
    public void testDeprecatedDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(83, 16));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        Map<String, String> expParams =
                Map.of("fname", "First name of the person",
                       "lname", "Last name of the person",
                       "street", "Street the person is living at",
                       "city", "The city the person is living in",
                       "countryCode", "The country code for the country the person is living in");
        Map<String, String> expDeprecatedParams = Map.of("street", "deprecated for removal",
                                                         "countryCode", "deprecated for removal");

        assertDocumentation(documentation.get(), "Creates and returns a `Person` object given the parameters.\n",
                            expParams, "A new Person string\n");
        assertEquals(documentation.get().deprecatedDescription().get(),
                     "This function is deprecated in favour of `Person` type.");
        assertEquals(documentation.get().deprecatedParametersMap().size(), expDeprecatedParams.size());
        expDeprecatedParams.forEach(
                (param, doc) -> assertEquals(documentation.get().deprecatedParametersMap().get(param), doc));
    }

    @Test
    public void testListenerDeclarationDocs() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(89, 22));
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertDescriptionOnly(documentation.get(), "This is a listener declaration");
    }

    // util methods

    private void assertDescriptionOnly(Documentation documentation, String description) {
        assertEquals(documentation.description().get(), description);
        assertEquals(documentation.parameterMap(), emptyMap);
        assertTrue(documentation.returnDescription().isEmpty());
    }

    private void assertDocumentation(Documentation documentation, String description, Map<String, String> params,
                                     String returnDesc) {
        assertEquals(documentation.description().get(), description);
        assertEquals(documentation.parameterMap().size(), params.size());
        params.forEach((param, doc) -> assertEquals(documentation.parameterMap().get(param), doc));

        if (returnDesc == null) {
            assertTrue(documentation.returnDescription().isEmpty());
        } else {
            assertEquals(documentation.returnDescription().get(), returnDesc);
        }
    }
}
