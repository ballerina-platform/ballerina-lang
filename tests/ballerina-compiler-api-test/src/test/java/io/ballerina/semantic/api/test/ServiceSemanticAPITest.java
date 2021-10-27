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
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.LiteralAttachPoint;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.Qualifier.FINAL;
import static io.ballerina.compiler.api.symbols.Qualifier.LISTENER;
import static io.ballerina.compiler.api.symbols.Qualifier.PUBLIC;
import static io.ballerina.compiler.api.symbols.Qualifier.RESOURCE;
import static io.ballerina.compiler.api.symbols.ServiceAttachPointKind.ABSOLUTE_RESOURCE_PATH;
import static io.ballerina.compiler.api.symbols.ServiceAttachPointKind.STRING_LITERAL;
import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class ServiceSemanticAPITest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/service_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testServiceClass() {
        ClassSymbol symbol = (ClassSymbol) model.symbol(srcFile, from(22, 14)).get();

        List<String> expMethods = List.of("foo", "get barPath", "get foo/path", "get .", "get foo/baz",
                                          "get foo/*", "get foo/*/**");
        assertList(symbol.methods(), expMethods);

        MethodSymbol method = symbol.methods().get("foo");
        assertEquals(method.qualifiers().size(), 1);
        assertTrue(method.qualifiers().contains(Qualifier.REMOTE));

        method = symbol.methods().get("get barPath");
        assertEquals(method.qualifiers().size(), 1);
        assertTrue(method.qualifiers().contains(Qualifier.RESOURCE));
    }

    @Test
    public void testServiceDeclTypedesc() {
        TypeSymbol symbol = (TypeSymbol) model.symbol(srcFile, from(66, 8)).get();
        assertEquals(symbol.typeKind(), TYPE_REFERENCE);
        assertEquals(symbol.getName().get(), "ProcessingService");
        assertEquals(((TypeReferenceTypeSymbol) symbol).typeDescriptor().typeKind(), OBJECT);
    }

    @Test
    public void testServiceDeclListener() {
        VariableSymbol symbol = (VariableSymbol) model.symbol(srcFile, from(66, 31)).get();
        assertEquals(symbol.getName().get(), "lsn");
        assertEquals(symbol.typeDescriptor().typeKind(), TYPE_REFERENCE);
        assertEquals(symbol.typeDescriptor().getName().get(), "Listener");
        assertEquals(symbol.qualifiers().size(), 2);
        assertTrue(symbol.qualifiers().contains(LISTENER));
        assertTrue(symbol.qualifiers().contains(FINAL));
    }

    @Test
    public void testServiceDeclField() {
        ClassFieldSymbol symbol = (ClassFieldSymbol) model.symbol(srcFile, from(68, 18)).get();
        assertEquals(symbol.getName().get(), "magic");
        assertEquals(symbol.typeDescriptor().typeKind(), STRING);
        assertEquals(symbol.qualifiers().size(), 1);
        assertTrue(symbol.qualifiers().contains(PUBLIC));
    }

    @Test(dataProvider = "ServiceDeclMethodPos")
    public void testServiceDeclMethods(int line, int col, String name, List<Qualifier> quals) {
        MethodSymbol symbol = (MethodSymbol) model.symbol(srcFile, from(line, col)).get();
        assertEquals(symbol.getName().get(), name);
        assertEquals(symbol.qualifiers().size(), quals.size());

        for (Qualifier qual : quals) {
            assertTrue(symbol.qualifiers().contains(qual));
        }
    }

    @DataProvider(name = "ServiceDeclMethodPos")
    public Object[][] getMethodPos() {
        return new Object[][]{
                {70, 22, "get", List.of(RESOURCE)},
                {72, 13, "createError", new ArrayList<Qualifier>()}
        };
    }

    @Test(dataProvider = "LookupPosProvider")
    public void testInScopeSymbolLookup(int line, int col, List<String> expSymNames) {
        List<Symbol> inscopeSymbols = model.visibleSymbols(srcFile, from(line, col));
        List<Symbol> sourceFileSymbols = getFilteredSymbolNames(inscopeSymbols);

        assertEquals(sourceFileSymbols.size(), expSymNames.size());
        assertList(sourceFileSymbols, expSymNames);
    }

    @DataProvider(name = "LookupPosProvider")
    public Object[][] getLookupPos() {
        List<String> moduleSymbols = List.of("AServiceType", "Listener", "lsn", "ProcessingService", "AServiceClass");
        return new Object[][]{
                {68, 26, moduleSymbols},
//                {70, 59, concatSymbols(moduleSymbols, "magic", "createError")} // TODO: Fix #27314
        };
    }

    @Test
    public void testServiceDeclSymbol() {
        Object[][] expVals = getExpValues();
        SemanticModel model = getDefaultModulesSemanticModel("test-src/service_decl_test.bal");
        List<ServiceDeclarationSymbol> services = model.moduleSymbols().stream()
                .filter(s -> s.kind() == SERVICE_DECLARATION)
                .map(s -> (ServiceDeclarationSymbol) s)
                .collect(Collectors.toList());

        assertEquals(services.size(), expVals.length);

        for (int i = 0, servicesSize = services.size(); i < servicesSize; i++) {
            ServiceDeclarationSymbol service = services.get(i);

            Optional<TypeSymbol> typedesc = service.typeDescriptor();
            if (typedesc.isPresent()) {
                assertEquals(typedesc.get().typeKind(), expVals[i][0]);
                assertEquals(typedesc.get().getName().get(), expVals[i][1]);
            } else {
                assertNull(expVals[i][0], "Expected typedesc kind: " + expVals[i][0]);
            }

            Optional<ServiceAttachPoint> attachPoint = service.attachPoint();
            if (attachPoint.isPresent()) {
                assertEquals(attachPoint.get().kind(), expVals[i][2]);
                String attachPointStr;
                if (attachPoint.get().kind() == ABSOLUTE_RESOURCE_PATH) {
                    attachPointStr = ((AbsResourcePathAttachPoint) attachPoint.get()).segments().toString();
                } else {
                    attachPointStr = ((LiteralAttachPoint) attachPoint.get()).literal();
                }
                assertEquals(attachPointStr, expVals[i][3]);
            } else {
                assertNull(expVals[i][2], "Expected attach point kind: " + expVals[i][2]);
            }
        }
    }

    @Test
    public void testResourceMethod1() {
        ResourceMethodSymbol method = (ResourceMethodSymbol) model.symbol(srcFile, from(35, 22)).get();
        assertEquals(method.resourcePath().kind(), ResourcePath.Kind.DOT_RESOURCE_PATH);
        assertEquals(method.resourcePath().signature(), ".");
        assertEquals(method.signature(), "resource function get . () returns string");
    }

    @Test
    public void testResourceMethod2() {
        ResourceMethodSymbol method = (ResourceMethodSymbol) model.symbol(srcFile, from(47, 22)).get();
        assertEquals(method.resourcePath().kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);
        assertEquals(method.resourcePath().signature(), "foo/[string s]/[string... r]");
        assertEquals(method.signature(), "resource function get foo/[string s]/[string... r] () returns string");

        PathSegmentList resourcePath = (PathSegmentList) method.resourcePath();

        List<PathParameterSymbol> pathParams = resourcePath.pathParameters();
        assertEquals(pathParams.size(), 1);
        assertEquals(pathParams.get(0).typeDescriptor().typeKind(), STRING);
        assertEquals(pathParams.get(0).getName().get(), "s");

        assertEquals(resourcePath.pathRestParameter().get().getName().get(), "r");
        assertEquals(resourcePath.pathRestParameter().get().typeDescriptor().typeKind(),
                     TypeDescKind.ARRAY);

        List<PathSegment> segments = resourcePath.list();
        assertEquals(segments.size(), 3);
        assertEquals(segments.get(0).pathSegmentKind(), PathSegment.Kind.NAMED_SEGMENT);
        assertEquals(((NamedPathSegment) segments.get(0)).name(), "foo");

        assertEquals(segments.get(1), pathParams.get(0));
        assertEquals(segments.get(2), resourcePath.pathRestParameter().get());
    }

    @Test
    public void testResourceMethod3() {
        ResourceMethodSymbol method = (ResourceMethodSymbol) model.symbol(srcFile, from(74, 22)).get();
        assertEquals(method.resourcePath().kind(), ResourcePath.Kind.PATH_REST_PARAM);
        assertEquals(method.resourcePath().signature(), "[int... rest]");
        assertEquals(method.signature(), "resource function get [int... rest] () returns string");

        PathRestParam resourcePath = (PathRestParam) method.resourcePath();
        assertEquals(resourcePath.parameter().getName().get(), "rest");
        assertEquals(resourcePath.parameter().typeDescriptor().typeKind(), ARRAY);
    }

    @Test
    public void testMultipleListenerAttaching() {
        SemanticModel model = getDefaultModulesSemanticModel("test-src/service_with_multiple_listeners.bal");
        List<Symbol> services = model.moduleSymbols().stream()
                .filter(s -> s.kind() == SERVICE_DECLARATION)
                .collect(Collectors.toList());
        ServiceDeclarationSymbol service = (ServiceDeclarationSymbol) services.get(0);
        List<TypeSymbol> listenerTypes = service.listenerTypes();
        assertEquals(listenerTypes.size(), 3);

        TypeSymbol type = listenerTypes.get(0);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "FooListener");

        type = listenerTypes.get(1);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "FooListener");

        type = listenerTypes.get(2);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "BarListener");
    }

    @Test
    public void testServiceDeclNegative() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(66, 34));
        assertTrue(symbol.isEmpty());
    }

    private Object[][] getExpValues() {
        return new Object[][]{
                {null, null, null, null},
                {TYPE_REFERENCE, "HelloWorld", ABSOLUTE_RESOURCE_PATH, "[]"},
                {TYPE_REFERENCE, "HelloWorld", ABSOLUTE_RESOURCE_PATH, "[foo, bar]"},
                {TYPE_REFERENCE, "HelloWorld", STRING_LITERAL, "GreetingService"},
        };
    }

    private List<Symbol> getFilteredSymbolNames(List<Symbol> symbols) {
        return symbols.stream()
                .filter(s -> s.getModule().isPresent() &&
                        !"ballerina".equals(s.getModule().get().getModule().get().id().orgName()))
                .collect(Collectors.toList());
    }
}
