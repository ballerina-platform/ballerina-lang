/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.compiler.api.impl.symbols.BallerinaPathParameterSymbol;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaDotResourcePath;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaPathRestParam;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaPathSegmentList;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for symbols in resource methods.
 *
 * @since 2201.8.0
 */
public class SymbolsInResourceMethodsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/resource_method_symbols.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "SymbolInResourceMethodPosProvider")
    public void testSymbolsInResourceMethod(int line, int col, String name, SymbolKind kind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, name, kind);
    }

    @DataProvider(name = "SymbolInResourceMethodPosProvider")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {17, 22, "get", SymbolKind.RESOURCE_METHOD},
                {24, 23, "get", SymbolKind.RESOURCE_METHOD},
                {24, 26, "authors", SymbolKind.PATH_NAME_SEGMENT},
                {28, 42, "author", SymbolKind.PARAMETER},
                {24, 45, "names", SymbolKind.PATH_PARAMETER},
                {32, 52, "isbn", SymbolKind.PATH_PARAMETER},
                {32, 80, "Book", SymbolKind.TYPE},
                {36, 27, null, SymbolKind.PATH_PARAMETER},
                {40, 37, "fl", SymbolKind.PATH_PARAMETER},
                {43, 36, null, SymbolKind.PATH_PARAMETER},
        };
    }

    @Test(dataProvider = "PathParamPosProvider")
    public void testPathParamSymbols(int line, int col, String name, PathSegment.Kind pathKind, TypeDescKind typekind) {
        PathParameterSymbol symbol = (PathParameterSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                                    name, SymbolKind.PATH_PARAMETER);
        assertEquals(symbol.pathSegmentKind(), pathKind);

        TypeSymbol typeDescriptor = symbol.typeDescriptor();
        assertEquals(typeDescriptor.typeKind(), typekind);
    }

    @DataProvider(name = "PathParamPosProvider")
    public Object[][] getPathParamPos() {
        return new Object[][]{
                {24, 45, "names", PathSegment.Kind.PATH_REST_PARAMETER, TypeDescKind.STRING},
                {32, 52, "isbn", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.UNION},
                {36, 48, "author", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.STRING},
                {36, 61, "bookId", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT},
        };
    }

    @Test
    public void testPathSegmentsInResourceMethod() {
        // function get registered/[float|decimal isbn]/[int... ex]()
        Object[][] expPathSegments = new Object[][]{
                {"registered", PathSegment.Kind.NAMED_SEGMENT},
                {"isbn", PathSegment.Kind.PATH_PARAMETER},
                {"ex", PathSegment.Kind.PATH_REST_PARAMETER},
        };

        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 32, 22, "get", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);

        List<PathSegment> pathSegments = ((BallerinaPathSegmentList) resourcePath).list();
        assertEquals(pathSegments.size(), expPathSegments.length);
        for (int i = 0; i < expPathSegments.length; i++) {
            assertPathSegment(pathSegments.get(i), (String) expPathSegments[i][0],
                    (PathSegment.Kind) expPathSegments[i][1]);
        }

        List<PathParameterSymbol> pathParamList = ((BallerinaPathSegmentList) resourcePath).pathParameters();
        assertEquals(pathParamList.size(), 1);
        PathParameterSymbol pathParam = pathParamList.get(0);
        assertPathSegment(pathParam, "isbn", PathSegment.Kind.PATH_PARAMETER);

        Optional<PathParameterSymbol> optPathSegment = ((BallerinaPathSegmentList) resourcePath).pathRestParameter();
        assertTrue(optPathSegment.isPresent());
        assertPathSegment(optPathSegment.get(), "ex", PathSegment.Kind.PATH_REST_PARAMETER);
    }

    @Test
    public void testPathSegmentsInResourceMethodWithTypeOnlyPathParam() {
        Object[][] expPathSegments = new Object[][]{
                {null, PathSegment.Kind.PATH_PARAMETER},
                {"author", PathSegment.Kind.PATH_PARAMETER},
                {"bookId", PathSegment.Kind.PATH_PARAMETER},
        };

        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 36, 22, "get", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);

        List<PathSegment> pathSegments = ((BallerinaPathSegmentList) resourcePath).list();
        assertEquals(pathSegments.size(), expPathSegments.length);
        for (int i = 0; i < expPathSegments.length; i++) {
            assertPathSegment(pathSegments.get(i), (String) expPathSegments[i][0],
                    (PathSegment.Kind) expPathSegments[i][1]);
        }
    }

    @Test
    public void testResourceMethodWithRestPathParam() {
        // function post [float... fl] ()
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 40, 22, "post", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_REST_PARAM);
        BallerinaPathParameterSymbol restPathParam = (BallerinaPathParameterSymbol)
                ((BallerinaPathRestParam) resourcePath).parameter();
        assertEquals(restPathParam.pathSegmentKind(), PathSegment.Kind.PATH_REST_PARAMETER);
        assertFalse(restPathParam.isTypeOnlyParam());
        assertTrue(restPathParam.getName().isPresent());
        assertEquals(restPathParam.getName().get(), "fl");
        TypeSymbol typeDesc = restPathParam.typeDescriptor();
        assertEquals(typeDesc.typeKind(), TypeDescKind.FLOAT);
    }

    @Test
    public void testResourceMethodWithTypeOnlyRestPathParam() {
        // function get [string...] ()
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 43, 22, "get", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_REST_PARAM);
        BallerinaPathParameterSymbol restPathParam = (BallerinaPathParameterSymbol)
                ((BallerinaPathRestParam) resourcePath).parameter();
        assertTrue(restPathParam.isTypeOnlyParam());
        assertTrue(restPathParam.getName().isEmpty());
        TypeSymbol typeDesc = restPathParam.typeDescriptor();
        assertEquals(typeDesc.typeKind(), TypeDescKind.STRING);
    }

    @Test
    public void testResourceMethodWithPathSegmentList() {
        // function get registered/[float|decimal isbn]/[int... ex]()
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 32, 22, "get", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);
        PathSegmentList pathSegmentList = (PathSegmentList) resourcePath;
        assertEquals(pathSegmentList.pathParameters().size(), 1);
        assertEquals(pathSegmentList.list().size(), 3);
    }

    @Test
    public void testResourceMethodWithDotPath() {
        // function get .()
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 17, 22, "get", SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.DOT_RESOURCE_PATH);
        BallerinaDotResourcePath dotResourcePath = (BallerinaDotResourcePath) resourcePath;
        assertEquals(dotResourcePath.signature(), ".");
    }

    @Test(dataProvider = "PathSegmentListInfo")
    public void testPathSegmentList(int line, int col, String methodName, List<ExpectedPathSegmentInfo> expPathSegments,
                                    List<ExpectedPathSegmentInfo> expPathParams,
                                    ExpectedPathSegmentInfo expRestPathParam) {
        PathSegmentList segmentList = assertResourceFunctionAndGetPathSegmentList(line, col, methodName);

        List<PathParameterSymbol> pathParams = segmentList.pathParameters();
        for (int i = 0; i < expPathParams.size(); i++) {
            assertPathParameter(pathParams.get(i), expPathParams.get(i).name, expPathParams.get(i).pathKind,
                    expPathParams.get(i).typeDescKind);
        }

        List<PathSegment> pathSegments = segmentList.list();
        for (int i = 0; i < expPathSegments.size(); i++) {
            assertPathSegment(pathSegments.get(i), expPathSegments.get(i).name, expPathSegments.get(i).pathKind);
        }

        assertTrue(segmentList.pathRestParameter().isPresent());
        assertPathParameter(segmentList.pathRestParameter().get(), expRestPathParam.name, expRestPathParam.pathKind,
                expRestPathParam.typeDescKind);
    }

    @DataProvider(name = "PathSegmentListInfo")
    public Object[][] getPathSegmentListInfo() {
        return new Object[][]{
                // function get books/[int year]/["AD"|"BC" era]/[string... authors]()
                {49, 22, "get",
                        List.of(
                                pathSegInfoFrom("books", PathSegment.Kind.NAMED_SEGMENT),
                                pathSegInfoFrom("year", PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom("authors", PathSegment.Kind.PATH_REST_PARAMETER)),
                        List.of(
                                pathSegInfoFrom("year", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.UNION)),
                        pathSegInfoFrom("authors", PathSegment.Kind.PATH_REST_PARAMETER, TypeDescKind.STRING)
                },

                // function put books/[int]/["AD"|"BC" era]/[string... authors]()
                {52, 22, "put",
                        List.of(
                                pathSegInfoFrom("books", PathSegment.Kind.NAMED_SEGMENT),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom("authors", PathSegment.Kind.PATH_REST_PARAMETER)),
                        List.of(
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.UNION)),
                        pathSegInfoFrom("authors", PathSegment.Kind.PATH_REST_PARAMETER, TypeDescKind.STRING)
                },

                // function post books/[int year]/["AD"|"BC" era]/[string...]()
                {55, 22, "post",
                        List.of(
                                pathSegInfoFrom("books", PathSegment.Kind.NAMED_SEGMENT),
                                pathSegInfoFrom("year", PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_REST_PARAMETER)),
                        List.of(
                                pathSegInfoFrom("year", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT),
                                pathSegInfoFrom("era", PathSegment.Kind.PATH_PARAMETER, TypeDescKind.UNION)),
                        pathSegInfoFrom(null, PathSegment.Kind.PATH_REST_PARAMETER, TypeDescKind.STRING)
                },

                // function patch books/[int]/["AD"|"BC"]/[string...]()
                {58, 22, "patch",
                        List.of(
                                pathSegInfoFrom("books", PathSegment.Kind.NAMED_SEGMENT),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_REST_PARAMETER)),
                        List.of(
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT),
                                pathSegInfoFrom(null, PathSegment.Kind.PATH_PARAMETER, TypeDescKind.UNION)),
                        pathSegInfoFrom(null, PathSegment.Kind.PATH_REST_PARAMETER, TypeDescKind.STRING)
                },
        };
    }

    @Test
    public void testAnnotAttachmentsInResourceMethod() {
        ResourceMethodSymbol symbol = (ResourceMethodSymbol)
                assertBasicsAndGetSymbol(model, srcFile, 66, 22, "get", SymbolKind.RESOURCE_METHOD);
        List<AnnotationAttachmentSymbol> annotAttachments = symbol.annotAttachments();
        assertEquals(annotAttachments.size(), 1);
        AnnotationAttachmentSymbol annotAttachment = annotAttachments.get(0);
        assertEquals(annotAttachment.kind(), SymbolKind.ANNOTATION_ATTACHMENT);
        AnnotationSymbol annotation = annotAttachment.typeDescriptor();
        assertTrue(annotation.getName().isPresent());
        assertEquals(annotation.getName().get(), "Pipe");
    }

    @Test
    public void testAnnotAttachmentInResourcePathParam() {
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol)
                assertBasicsAndGetSymbol(model, srcFile, 69, 22, "get", SymbolKind.RESOURCE_METHOD);
        PathSegmentList resourcePath = (PathSegmentList) resourceMethod.resourcePath();
        List<PathParameterSymbol> pathParams = resourcePath.pathParameters();
        assertEquals(pathParams.size(), 1);
        PathParameterSymbol param = pathParams.get(0);
        assertEquals(param.annotAttachments().size(), 1);
        AnnotationAttachmentSymbol annotAttachment = param.annotAttachments().get(0);
        assertEquals(annotAttachment.kind(), SymbolKind.ANNOTATION_ATTACHMENT);
        AnnotationSymbol annotation = annotAttachment.typeDescriptor();
        assertTrue(annotation.getName().isPresent());
        assertEquals(annotation.getName().get(), "Pipe");
    }

    @Test
    public void testAnnotAttachmentInResourcePathRestParam() {
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol)
                assertBasicsAndGetSymbol(model, srcFile, 72, 22, "get", SymbolKind.RESOURCE_METHOD);
        PathSegmentList resourcePath = (PathSegmentList) resourceMethod.resourcePath();
        Optional<PathParameterSymbol> pathParams = resourcePath.pathRestParameter();
        assertTrue(pathParams.isPresent());
        PathParameterSymbol param = pathParams.get();
        assertEquals(param.annotAttachments().size(), 1);
        AnnotationAttachmentSymbol annotAttachment = param.annotAttachments().get(0);
        assertEquals(annotAttachment.kind(), SymbolKind.ANNOTATION_ATTACHMENT);
        AnnotationSymbol annotation = annotAttachment.typeDescriptor();
        assertTrue(annotation.getName().isPresent());
        assertEquals(annotation.getName().get(), "Pipe");
    }

    private static class ExpectedPathSegmentInfo {
        String name;
        PathSegment.Kind pathKind;
        TypeDescKind typeDescKind;

        private ExpectedPathSegmentInfo(String name, PathSegment.Kind pathKind, TypeDescKind typeDescKind) {
            this.name = name;
            this.pathKind = pathKind;
            this.typeDescKind = typeDescKind;
        }
    }

    private static ExpectedPathSegmentInfo pathSegInfoFrom(String name, PathSegment.Kind pathKind,
                                                           TypeDescKind typeDescKind) {
        return new ExpectedPathSegmentInfo(name, pathKind, typeDescKind);
    }

    private static ExpectedPathSegmentInfo pathSegInfoFrom(String name, PathSegment.Kind pathKind) {
        return new ExpectedPathSegmentInfo(name, pathKind, null);
    }

    private PathSegmentList assertResourceFunctionAndGetPathSegmentList(int line, int col, String funcName) {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, line, col, funcName, SymbolKind.RESOURCE_METHOD);
        ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) symbol;
        ResourcePath resourcePath = resourceMethod.resourcePath();
        assertEquals(resourcePath.kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);
        return (PathSegmentList) resourcePath;
    }

    private void assertPathSegment(PathSegment pathSegment, String expName, PathSegment.Kind expKind) {
        assertEquals(pathSegment.pathSegmentKind(), expKind);
        if (expName != null) {
            assertTrue(pathSegment.getName().isPresent());
            assertEquals(pathSegment.getName().get(), expName);
        } else {
            assertTrue(pathSegment.getName().isEmpty());
        }
    }

    private void assertPathParameter(PathParameterSymbol pathParameter, String expName, PathSegment.Kind expKind,
                                     TypeDescKind expTypeKind) {
        assertEquals(pathParameter.pathSegmentKind(), expKind);
        assertEquals(pathParameter.typeDescriptor().typeKind(), expTypeKind);
        if (expName != null) {
            assertTrue(pathParameter.getName().isPresent());
            assertEquals(pathParameter.getName().get(), expName);
        } else {
            assertTrue(pathParameter.getName().isEmpty());
        }
    }
}
