// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.semantic.api.test.actions;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaPathSegmentList;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
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

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with resource access action.
 *
 * @since 2201.2.0
 */
public class SymbolsInResourceAccessActionTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/actions/resource_access_action.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ResourceAccessActionPositionAndPathSegments")
    public void testResourceAccessAction(int line, int col, String name, Map<String, PathSegment.Kind> pathSegments) {
        ResourceMethodSymbol sym = (ResourceMethodSymbol)
                assertBasicsAndGetSymbol(model, srcFile, line, col, name, SymbolKind.RESOURCE_METHOD);
        ResourcePath resourcePath = sym.resourcePath();
        assertPathSegment(pathSegments, ((BallerinaPathSegmentList) resourcePath).list());
        FunctionTypeSymbol functionTypeSymbol = sym.typeDescriptor();
        assertEquals(functionTypeSymbol.typeKind(), TypeDescKind.FUNCTION);
    }

    @DataProvider(name = "ResourceAccessActionPositionAndPathSegments")
    public Object[][] getResourceAccessActionPosAndPathSegments() {
        return new Object[][]{
                {42, 34, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "bar", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {43, 25, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {44, 39, "post", Map.of(
                        "bar", PathSegment.Kind.NAMED_SEGMENT,
                        "[string... ids]", PathSegment.Kind.PATH_REST_PARAMETER)},
                {66, 11, "get", Map.of(
                        "[string s1]", PathSegment.Kind.PATH_PARAMETER,
                        "[string s2]", PathSegment.Kind.PATH_PARAMETER)},
                {76, 11, "get", Map.of(
                        "path1", PathSegment.Kind.NAMED_SEGMENT,
                        "path2", PathSegment.Kind.NAMED_SEGMENT)},
        };
    }

    @Test(dataProvider = "SymbolPosInResourceAccessAction")
    public void testSymbolsInResourceAccessAction(int line, int col, String name, SymbolKind symbolKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        if (name == null) {
            assertTrue(symbol.isEmpty());
            return;
        }
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), symbolKind);
        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "SymbolPosInResourceAccessAction")
    public Object[][] getSymbolPosInResourceAccessAction() {
        return new Object[][]{
                {42, 23, "fooClient", SymbolKind.VARIABLE}, // <c>fooClient->/foo
                {42, 32, "get", SymbolKind.RESOURCE_METHOD},   // -><c>/foo
                {42, 34, "get", SymbolKind.RESOURCE_METHOD},    // -><c>/
                {42, 35, "foo", SymbolKind.PATH_NAME_SEGMENT}, // ->/<c>foo
                {42, 49, "x", SymbolKind.CONSTANT},  // ["3"](<c>x, 2);
                {78, 10, "get", SymbolKind.RESOURCE_METHOD}  // -><c>/()
        };
    }

    @Test(dataProvider = "ResourceAccessActionPosWithTargetFunctionSignature")
    public void testTargetFunctionOfResourceAccessAction(int line, int col, String signature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.RESOURCE_METHOD);
        assertEquals(((ResourceMethodSymbol) symbol.get()).signature(), signature);
    }

    @DataProvider(name = "ResourceAccessActionPosWithTargetFunctionSignature")
    public Object[][] getResourceAccessActionPosAndFunctionSignatures() {
        return new Object[][]{
                // Resource method: [string s1]/[string s2]()
                {66, 10, "resource function get [string s1]/[string s2] () returns ()"},
                {67, 10, "resource function get [string s1]/[string s2] () returns ()"},
                {68, 10, "resource function get [string s1]/[string s2] () returns ()"},

                // Resource method: [string... ss]()
                {70, 10, "resource function get [string... ss] () returns ()"},
                {71, 10, "resource function get [string... ss] () returns ()"},
                {72, 10, "resource function get [string... ss] () returns ()"},

                // Resource method: path1/path2()
                {74, 10, "resource function get path1/path2 () returns ()"},
                {76, 10, "resource function get path1/path2 () returns ()"},

                // Resource method: .()
                {78, 10, "resource function get . () returns ()"},
        };
    }

    @Test(dataProvider = "ResourceAccessActionPathPos")
    public void testResourceAccessActionPath2(int line, int col, String signature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.PATH_NAME_SEGMENT);
        assertEquals(((PathSegment) symbol.get()).signature(), signature);
    }

    @DataProvider(name = "ResourceAccessActionPathPos")
    public Object[][] getResourceAccessActionPathPos() {
        return new Object[][]{
                // Resource method: path1/path2()
                {74, 11, "path1"},   // quxCl->/<CURSOR>path1/path2()
                {76, 12, "path1"},   // quxCl->/[<CURSOR>"path1"]/["path2"]()
        };
    }

    @Test
    public void testPathSegmentOfAmbiguousResourceFunction() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(91, 9));
        assertTrue(symbol.isPresent());
    }

    // Utils
    private void assertPathSegment(Map<String, PathSegment.Kind> expected, List<PathSegment> actual) {
        expected.forEach((expName, expKind) ->
                assertTrue(actual.stream()
                        .anyMatch(actualName ->
                                actualName.signature().equals(expName) && actualName.pathSegmentKind().equals(expKind)))
        );
    }
}
