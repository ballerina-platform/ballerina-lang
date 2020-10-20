/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.types.util.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.types.util.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.types.util.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.types.util.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.types.util.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.types.util.TypeDescKind.INT;
import static io.ballerina.compiler.api.types.util.TypeDescKind.MAP;
import static io.ballerina.compiler.api.types.util.TypeDescKind.NIL;
import static io.ballerina.compiler.api.types.util.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.types.util.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.types.util.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.types.util.TypeDescKind.STRING;
import static io.ballerina.compiler.api.types.util.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.types.util.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.types.util.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.types.util.TypeDescKind.UNION;
import static io.ballerina.compiler.api.types.util.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.compile;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the functions returned from the langLibMethods() method in a type.
 *
 * @since 2.0.0
 */
public class LangLibFunctionTest {

    private static List<String> valueLangLib = List.of("clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                                                       "toString", "toBalString", "toJson", "toJsonString",
                                                       "fromJsonString", "fromJsonFloatString", "fromJsonDecimalString",
                                                       "fromJsonWithType", "fromJsonStringWithType", "mergeJson");
    private SemanticModel model;

    @BeforeClass
    public void setup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/langlib_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
    }

    @Test
    public void testIntLangLib() {
        Symbol symbol = getSymbol(43, 8);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), INT);

        List<String> expFunctions = List.of("abs", "max", "min", "toHexString", "clone", "cloneReadOnly",
                                            "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson",
                                            "toJsonString", "fromJsonWithType", "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testFloatLangLib() {
        Symbol symbol = getSymbol(16, 7);
        BallerinaTypeDescriptor type = ((ConstantSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), FLOAT);

        List<String> expFunctions = List.of("isFinite", "isInfinite", "isNaN", "abs", "round", "floor", "ceiling",
                                            "sqrt", "cbrt", "pow", "log", "log10", "exp", "sin", "cos", "tan", "asin",
                                            "acos", "atan", "atan2", "sinh", "cosh", "tanh", "toHexString", "toBitsInt",
                                            "clone", "cloneReadOnly", "cloneWithType", "isReadOnly", "toString",
                                            "toBalString", "toJson", "toJsonString", "fromJsonWithType", "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testDecimalLangLib() {
        Symbol symbol = getSymbol(58, 12);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), DECIMAL);

        List<String> expFunctions = List.of("abs", "max", "min", "round", "floor", "ceiling", "clone", "cloneReadOnly",
                                            "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson",
                                            "toJsonString", "fromJsonWithType", "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testStringLangLib() {
        Symbol symbol = getSymbol(19, 11);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), STRING);

        List<String> expFunctions = List.of("length", "iterator", "getCodePoint", "substring", "codePointCompare",
                                            "join", "indexOf", "lastIndexOf", "startsWith", "endsWith", "toLowerAscii",
                                            "toUpperAscii", "equalsIgnoreCaseAscii", "trim", "toBytes",
                                            "toCodePointInts", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                                            "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType",
                                            "mergeJson", "fromJsonString", "fromJsonFloatString",
                                            "fromJsonDecimalString", "fromJsonStringWithType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testFutureLangLib() {
        Symbol symbol = getSymbol(45, 16);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), FUTURE);

        List<String> expFunctions = List.of("cancel", "toString", "toBalString");
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testArrayLangLib() {
        Symbol symbol = getSymbol(47, 18);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), ARRAY);

        List<String> expFunctions = List.of("length", "iterator", "enumerate", "map", "forEach", "filter", "reduce",
                                            "slice", "remove", "removeAll", "setLength", "indexOf", "lastIndexOf",
                                            "reverse", "sort", "pop", "push", "shift", "unshift", "toString",
                                            "toBalString", "toStream");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testMapLangLib() {
        Symbol symbol = getSymbol(49, 16);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), MAP);

        List<String> expFunctions = List.of("length", "iterator", "get", "entries", "map", "forEach", "filter",
                                            "reduce", "removeIfHasKey", "remove", "removeAll", "hasKey", "keys",
                                            "toArray", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                                            "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType",
                                            "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(dataProvider = "XMLInfoProvider")
    public void testXMLLangLib(int line, int column, List<String> expFunctions) {
        Symbol symbol = getSymbol(67, 8);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), XML);
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @DataProvider(name = "XMLInfoProvider")
    public Object[][] getXMLInfo() {
        List<String> expFunctions = List.of("length", "iterator", "forEach", "map", "filter", "get", "slice", "strip",
                                            "elements", "children", "elementChildren", "clone", "cloneReadOnly",
                                            "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson",
                                            "toJsonString");

//        List<String> additionalFuncs = List.of("getName", "setName", "getChildren", "setChildren", "getAttributes");

        return new Object[][]{
                {67, 8, expFunctions},
//                {68, 17, Stream.concat(expFunctions.stream(), additionalFuncs.stream()).collect(Collectors.toList())}
        };
    }

    @Test
    public void testValueLangLib() {
        Symbol symbol = getSymbol(38, 9);
        FunctionTypeDescriptor type = (FunctionTypeDescriptor) ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.returnTypeDescriptor().get().kind(), NIL);

        List<String> expFunctions = List.of("clone", "cloneReadOnly", "cloneWithType", "isReadOnly", "toString",
                                            "toBalString", "toJson", "toJsonString", "fromJsonWithType", "mergeJson");

        assertLangLibList(type.returnTypeDescriptor().get().langLibMethods(), expFunctions);
    }

    @Test
    public void testObjectLangLib() {
        Symbol symbol = getSymbol(28, 6);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((TypeSymbol) symbol).typeDescriptor();
        ObjectTypeDescriptor type = (ObjectTypeDescriptor) typeRef.typeDescriptor();
        assertEquals(type.kind(), OBJECT);

        List<String> expFunctions = List.of("toString", "toBalString");
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testRecordLangLib() {
        Symbol symbol = getSymbol(18, 5);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((TypeSymbol) symbol).typeDescriptor();
        RecordTypeDescriptor type = (RecordTypeDescriptor) typeRef.typeDescriptor();
        assertEquals(type.kind(), RECORD);

        List<String> expFunctions = List.of("length", "iterator", "get", "entries", "map", "forEach", "filter",
                                            "reduce", "removeIfHasKey", "remove", "removeAll", "hasKey", "keys",
                                            "toArray", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                                            "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType",
                                            "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(enabled = false)
    public void testTupleLangLib() {
        Symbol symbol = getSymbol(51, 28);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TUPLE);

        List<String> expFunctions = List.of("length", "iterator", "enumerate", "map", "forEach", "filter", "reduce",
                                            "slice", "remove", "removeAll", "setLength", "indexOf", "lastIndexOf",
                                            "reverse", "sort", "pop", "push", "shift", "unshift", "toStream", "clone",
                                            "cloneReadOnly", "cloneWithType", "isReadOnly", "toString", "toBalString",
                                            "toJson", "toJsonString", "fromJsonWithType", "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testTypedescLangLib() {
        Symbol symbol = getSymbol(53, 22);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TYPEDESC);

        List<String> expFunctions = List.of("toString", "toBalString");
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testUnionType() {
        Symbol symbol = getSymbol(56, 21);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), UNION);

        List<String> expFunctions = List.of("clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                                            "toString", "toBalString", "toJson", "toJsonString",
                                            "fromJsonWithType", "mergeJson");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testErrorLangLib() {
        Symbol symbol = getSymbol(60, 10);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), ERROR);

        List<String> expFunctions = List.of("message", "cause", "detail", "stackTrace", "toString", "toBalString");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testStreamLangLib() {
        Symbol symbol = getSymbol(62, 22);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), STREAM);

        List<String> expFunctions = List.of("filter", "next", "map", "reduce", "forEach", "iterator", "close",
                                            "toString", "toBalString");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(dataProvider = "TableInfoProvider")
    public void testTableLangLib(int line, int column, List<String> expFunctions) {
        Symbol symbol = getSymbol(line, column);
        BallerinaTypeDescriptor type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TABLE);
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @DataProvider(name = "TableInfoProvider")
    public Object[][] getTableInfo() {
        List<String> expFunctions = List.of("length", "put", "add", "removeAll", "toArray", "map", "reduce", "forEach",
                                            "iterator", "toString", "toBalString", "clone", "cloneReadOnly",
                                            "cloneWithType", "isReadOnly", "toJson", "toJsonString");

        List<String> additionalFuncs = List.of("get", "remove", "removeIfHasKey", "filter", "hasKey", "keys");

        return new Object[][]{
                {64, 22, expFunctions},
                {65, 32, Stream.concat(expFunctions.stream(), additionalFuncs.stream()).collect(Collectors.toList())}
        };
    }

    private Symbol getSymbol(int line, int column) {
        return model.symbol("langlib_test.bal", from(line, column)).get();
    }

    private void assertLangLibList(List<MethodSymbol> langLib, List<String> expFunctions) {
        Set<String> langLibSet = langLib.stream().map(MethodSymbol::name).collect(Collectors.toSet());

        for (String expFunction : expFunctions) {
            assertTrue(langLibSet.contains(expFunction), "Expected function '" + expFunction + "' not found");
        }

        assertEquals(langLibSet.size(), expFunctions.size());
    }
}
