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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.langlib;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for type param bounded lang lib function tests for xml.
 */
public class TypeParamBoundXMLFunctionsTest {

    private SemanticModel model;
    private Map<String, FunctionSymbol> langlibFns;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/type_param_bound_langlib_functions.bal");
        model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(22, 21));

        assertTrue(symbol.isPresent());
        TypeSymbol typeSymbol = ((VariableSymbol) symbol.get()).typeDescriptor();
        langlibFns = typeSymbol.langLibMethods().stream().collect(Collectors.toMap(fn -> fn.getName().get(), fn -> fn));
    }

    @Test
    public void testIterator() {
        FunctionTypeSymbol iteratorFnType = assertFnNameAndGetParams("iterator");
        List<ParameterSymbol> params = iteratorFnType.params().get();

        assertEquals(params.size(), 1);
        assertParam(params.get(0));
    }

    @Test
    public void testGet() {
        FunctionTypeSymbol getFnType = assertFnNameAndGetParams("get");
        List<ParameterSymbol> params = getFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        assertTrue(getFnType.returnTypeDescriptor().isPresent());

        TypeSymbol getFnRetType = getFnType.returnTypeDescriptor().get();
        assertEquals(getFnRetType.typeKind(), TypeDescKind.XML);
    }

    @Test
    public void testSlice() {
        FunctionTypeSymbol sliceFnType = assertFnNameAndGetParams("slice");
        List<ParameterSymbol> params = sliceFnType.params().get();

        assertEquals(params.size(), 3);
        assertParam(params.get(0));

        TypeSymbol sliceFnRetType = sliceFnType.returnTypeDescriptor().get();
        assertEquals(sliceFnRetType.typeKind(), TypeDescKind.XML);
        assertTypeRef(((XMLTypeSymbol) sliceFnRetType).typeParameter().get());
    }

    @Test
    public void testMap() {
        FunctionTypeSymbol mapFnType = assertFnNameAndGetParams("map");
        List<ParameterSymbol> params = mapFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        TypeSymbol paramType = params.get(1).typeDescriptor();
        assertEquals(paramType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) paramType;
//        assertEquals(fnType.params().get().size(), 1);
//        assertEquals(fnType.params().get().get(0).typeDescriptor().typeKind(), TypeDescKind.INT);
//        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), TypeDescKind.UNION);

        TypeSymbol mapFnRetType = mapFnType.returnTypeDescriptor().get();
        assertEquals(mapFnRetType.typeKind(), TypeDescKind.XML);
        assertEquals(((XMLTypeSymbol) mapFnRetType).typeParameter().get().typeKind(), TypeDescKind.XML);
    }

    @Test
    public void testForEach() {
        FunctionTypeSymbol foreachFnType = assertFnNameAndGetParams("forEach");
        List<ParameterSymbol> params = foreachFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        TypeSymbol paramType = params.get(1).typeDescriptor();
        assertEquals(paramType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) paramType;
//        assertEquals(fnType.params().get().size(), 1);
//        assertEquals(fnType.params().get().get(0).typeDescriptor().typeKind(), TypeDescKind.INT);
//        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), TypeDescKind.UNION);
    }

    @Test
    public void testFilter() {
        FunctionTypeSymbol filterFnType = assertFnNameAndGetParams("filter");
        List<ParameterSymbol> params = filterFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        TypeSymbol paramType = params.get(1).typeDescriptor();
        assertEquals(paramType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) paramType;
//        assertEquals(fnType.params().get().size(), 1);
//        assertEquals(fnType.params().get().get(0).typeDescriptor().typeKind(), TypeDescKind.INT);
//        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), TypeDescKind.UNION);

        TypeSymbol filterFnRetType = filterFnType.returnTypeDescriptor().get();
        assertEquals(filterFnRetType.typeKind(), TypeDescKind.XML);
    }

    private FunctionTypeSymbol assertFnNameAndGetParams(String fnName) {
        assertTrue(langlibFns.containsKey(fnName));
        FunctionSymbol fn = langlibFns.get(fnName);
        return fn.typeDescriptor();
    }

    private void assertParam(ParameterSymbol param) {
        TypeSymbol typeSymbol = param.typeDescriptor();
        assertEquals(typeSymbol.typeKind(), TypeDescKind.XML);
        assertTypeRef(((XMLTypeSymbol) typeSymbol).typeParameter().get());
    }

    private void assertTypeRef(TypeSymbol type) {
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Element");
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), TypeDescKind.XML_ELEMENT);
    }
}
