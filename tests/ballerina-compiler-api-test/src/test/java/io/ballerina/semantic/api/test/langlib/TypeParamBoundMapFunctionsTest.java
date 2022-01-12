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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
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
 * Tests for type param bounded lang lib function tests for maps.
 */
public class TypeParamBoundMapFunctionsTest {

    private SemanticModel model;
    private Map<String, FunctionSymbol> langlibFns;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/type_param_bound_langlib_functions.bal");
        model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(19, 15));

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
        assertEquals(getFnRetType.typeKind(), TypeDescKind.FLOAT);
    }

    @Test
    public void testEntries() {
        FunctionTypeSymbol enumerateFnType = assertFnNameAndGetParams("entries");
        List<ParameterSymbol> params = enumerateFnType.params().get();

        assertEquals(params.size(), 1);
        assertParam(params.get(0));

        assertTrue(enumerateFnType.returnTypeDescriptor().isPresent());

        TypeSymbol returnType = enumerateFnType.returnTypeDescriptor().get();
        assertEquals(returnType.typeKind(), TypeDescKind.MAP);

        TypeSymbol elemType = ((MapTypeSymbol) returnType).typeParam();
        assertEquals(elemType.typeKind(), TypeDescKind.TUPLE);

        List<TypeSymbol> tupMembers = ((TupleTypeSymbol) elemType).memberTypeDescriptors();
        assertEquals(tupMembers.get(0).typeKind(), TypeDescKind.STRING);
        assertEquals(tupMembers.get(1).typeKind(), TypeDescKind.FLOAT);
    }

    @Test
    public void testMap() {
        FunctionTypeSymbol mapFnType = assertFnNameAndGetParams("'map");
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
        assertEquals(mapFnRetType.typeKind(), TypeDescKind.MAP);

        TypeSymbol typeParameterSymbol = ((MapTypeSymbol) mapFnRetType).typeParam();
        assertEquals(typeParameterSymbol.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) typeParameterSymbol).typeDescriptor().typeKind(), TypeDescKind.UNION);
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
        assertEquals(filterFnRetType.typeKind(), TypeDescKind.MAP);
        assertEquals(((MapTypeSymbol) filterFnRetType).typeParam().typeKind(), TypeDescKind.FLOAT);
    }

    @Test
    public void testReduce() {
        FunctionTypeSymbol reduceFnType = assertFnNameAndGetParams("reduce");
        List<ParameterSymbol> params = reduceFnType.params().get();

        assertEquals(params.size(), 3);
        assertParam(params.get(0));

        TypeSymbol paramType = params.get(1).typeDescriptor();
        assertEquals(paramType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) paramType;
//        assertEquals(fnType.params().get().size(), 1);
//        assertEquals(fnType.params().get().get(0).typeDescriptor().typeKind(), TypeDescKind.INT);
//        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), TypeDescKind.UNION);

        TypeSymbol parameterTypeSymbol = params.get(2).typeDescriptor();
        assertEquals(parameterTypeSymbol.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) parameterTypeSymbol).typeDescriptor().typeKind(), TypeDescKind.UNION);

        TypeSymbol pushFnRetType = reduceFnType.returnTypeDescriptor().get();
        assertEquals(pushFnRetType.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) pushFnRetType).typeDescriptor().typeKind(), TypeDescKind.UNION);
    }

    @Test
    public void testRemove() {
        FunctionTypeSymbol removeFnType = assertFnNameAndGetParams("remove");
        List<ParameterSymbol> params = removeFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        TypeSymbol removeFnRetType = removeFnType.returnTypeDescriptor().get();
        assertEquals(removeFnRetType.typeKind(), TypeDescKind.FLOAT);
    }

    @Test
    public void testRemoveIfHasKey() {
        FunctionTypeSymbol removeFnType = assertFnNameAndGetParams("removeIfHasKey");
        List<ParameterSymbol> params = removeFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));

        TypeSymbol removeFnRetType = removeFnType.returnTypeDescriptor().get();
        assertEquals(removeFnRetType.typeKind(), TypeDescKind.UNION);

        List<TypeSymbol> members = ((UnionTypeSymbol) removeFnRetType).memberTypeDescriptors();
        assertEquals(members.get(0).typeKind(), TypeDescKind.FLOAT);
        assertEquals(members.get(1).typeKind(), TypeDescKind.NIL);
    }

    @Test
    public void testHasKey() {
        FunctionTypeSymbol hasKeyFnType = assertFnNameAndGetParams("hasKey");
        List<ParameterSymbol> params = hasKeyFnType.params().get();

        assertEquals(params.size(), 2);
        assertParam(params.get(0));
    }

    @Test
    public void testToArray() {
        FunctionTypeSymbol toArrayFnType = assertFnNameAndGetParams("toArray");
        List<ParameterSymbol> params = toArrayFnType.params().get();

        assertEquals(params.size(), 1);
        assertParam(params.get(0));

        TypeSymbol toArrayFnRetType = toArrayFnType.returnTypeDescriptor().get();
        assertEquals(toArrayFnRetType.typeKind(), TypeDescKind.ARRAY);
        assertEquals(((ArrayTypeSymbol) toArrayFnRetType).memberTypeDescriptor().typeKind(), TypeDescKind.FLOAT);
    }

    private FunctionTypeSymbol assertFnNameAndGetParams(String fnName) {
        assertTrue(langlibFns.containsKey(fnName));
        FunctionSymbol fn = langlibFns.get(fnName);
        return fn.typeDescriptor();
    }

    private void assertParam(ParameterSymbol param) {
        TypeSymbol typeSymbol = param.typeDescriptor();
        assertEquals(typeSymbol.typeKind(), TypeDescKind.MAP);
        assertEquals(((MapTypeSymbol) typeSymbol).typeParam().typeKind(), TypeDescKind.FLOAT);
    }
}
