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

    @Test(dataProvider = "ResourceAccessActionPosition")
    public void testResourceAccessAction(int line, int col, String name, Map<String, PathSegment.Kind> pathSegments) {
        ResourceMethodSymbol sym = (ResourceMethodSymbol)
                assertBasicsAndGetSymbol(model, srcFile, line, col, name, SymbolKind.RESOURCE_METHOD);
        ResourcePath resourcePath = sym.resourcePath();
        assertPathSegment(pathSegments, ((BallerinaPathSegmentList) resourcePath).list());
        FunctionTypeSymbol functionTypeSymbol = sym.typeDescriptor();
        assertEquals(functionTypeSymbol.typeKind(), TypeDescKind.FUNCTION);
    }

    @DataProvider(name = "ResourceAccessActionPosition")
    public Object[][] getResourceAccessActionPosAndPathSegments() {
        return new Object[][]{
                {42, 34, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "bar", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {43, 25, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {44, 28, "post", Map.of(
                        "bar", PathSegment.Kind.NAMED_SEGMENT,
                        "[string... ids]", PathSegment.Kind.PATH_REST_PARAMETER)},
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
                {42, 32, null, null},   // -><c>/foo
                {42, 34, "get", SymbolKind.RESOURCE_METHOD},    // <c>->
                {42, 35, "foo", SymbolKind.PATH_SEGMENT}, // ->/<c>foo
                {42, 49, "x", SymbolKind.CONSTANT}  // ["3"](<c>x, 2);
        };
    }

    @Test(dataProvider = "ResourceAccessActionPathPos1")
    public void testResourceAccessActionPath1(int line, int col, String signature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.RESOURCE_METHOD);
        assertEquals(((ResourceMethodSymbol) symbol.get()).signature(), signature);
    }

    @DataProvider(name = "ResourceAccessActionPathPos1")
    public Object[][] getResourceAccessActionPathPos1() {
        return new Object[][]{
                // Resource method: [string s1]/[string s2]()
                {66, 12, "resource function get [string s1]/[string s2] () returns ()"},   // barCl->/<CURSOR>seg1/seg2()
                {67, 17, "resource function get [string s1]/[string s2] () returns ()"},   // barCl->[...[<CURSOR>"seg1", "seg2"]]()
                {68, 13, "resource function get [string s1]/[string s2] () returns ()"},   // barCl->/[<CURSOR>"seg1"]/["seg2"]()

                // Resource method: [string... ss]()
                {70, 12, "resource function get [string... ss] () returns ()"},   // bazCl->/<CURSOR>seg1/seg2()
                {71, 17, "resource function get [string... ss] () returns ()"},   // bazCl->[...[<CURSOR>"seg1", "seg2"]]()
                {72, 13, "resource function get [string... ss] () returns ()"},   // bazCl->/[<CURSOR>"seg1"]/["seg2"]()
        };
    }

    @Test(dataProvider = "ResourceAccessActionPathPos2")
    public void testResourceAccessActionPath2(int line, int col, String signature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.PATH_SEGMENT);
        assertEquals(((PathSegment) symbol.get()).signature(), signature);
    }

    @DataProvider(name = "ResourceAccessActionPathPos2")
    public Object[][] getResourceAccessActionPathPos2() {
        return new Object[][]{
                // Resource method: path1/path2()
                {74, 12, "path1"},   // quxCl->/<CURSOR>path1/path2()
//                {75, 17, ""},   // quxCl->[...[<CURSOR>"path1", "path2"]]() // TODO: Remove this invalid test case
                {76, 13, "path1"},   // quxCl->/[<CURSOR>"path1"]/["path2"]()

                // Resource method: get . ()
//                {81, 13, ""},   // quxCl->/<CURSOR>()  TODO: Remove if this is not needed to evaluate
        };
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
