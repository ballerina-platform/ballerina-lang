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
                {40, 34, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "bar", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {41, 25, "get", Map.of(
                        "foo", PathSegment.Kind.NAMED_SEGMENT,
                        "[string id]", PathSegment.Kind.PATH_PARAMETER)},
                {42, 28, "post", Map.of(
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
                {40, 23, "fooClient", SymbolKind.VARIABLE}, // <c>fooClient->/foo
                {40, 32, null, null},   // -><c>/foo
                {40, 34, "get", SymbolKind.RESOURCE_METHOD},    // <c>->
//                {40, 35, "foo", <undefined>}, // ->/<c>foo    //TODO: Enable this after fixing #37604
                {40, 49, "x", SymbolKind.CONSTANT}  // ["3"](<c>x, 2);
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
