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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the functions returned from the langLibMethods() method in a type.
 *
 * @since 2.0.0
 */
public class LangLibFunctionTest {

    private static List<String> valueLangLib = List.of("clone", "cloneReadOnly", "cloneWithType",
            "isReadOnly", "toString", "toBalString", "toJson", "toJsonString",
            "fromJsonString", "fromJsonFloatString", "fromJsonDecimalString",
            "fromJsonWithType", "fromJsonStringWithType", "mergeJson");
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/langlib_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testIntLangLib() {
        Symbol symbol = getSymbol(43, 8);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INT);

        List<String> expFunctions = List.of("abs", "max", "min", "sum", "toHexString", "clone",
                "cloneReadOnly", "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson",
                "toJsonString", "fromJsonWithType", "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testFloatLangLib() {
        Symbol symbol = getSymbol(16, 7);
        TypeSymbol type = ((ConstantSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), SINGLETON);

        List<String> expFunctions = List.of("isFinite", "isInfinite", "isNaN", "abs", "round", "floor",
                "ceiling", "sqrt", "cbrt", "pow", "log", "log10", "exp", "sin", "cos", "tan", "asin",
                "acos", "atan", "atan2", "sinh", "cosh", "tanh", "toHexString", "toBitsInt",
                "clone", "cloneReadOnly", "cloneWithType", "isReadOnly", "toString",
                "toBalString", "toJson", "toJsonString", "fromJsonWithType", "mergeJson",
                "ensureType", "sum", "min", "max", "toFixedString", "toExpString");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testDecimalLangLib() {
        Symbol symbol = getSymbol(58, 12);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), DECIMAL);

        List<String> expFunctions = List.of("abs", "max", "min", "sum", "round", "floor", "ceiling", "clone",
                "cloneReadOnly", "cloneWithType", "isReadOnly", "toString", "toBalString", "quantize",
                "toJson", "toJsonString", "fromJsonWithType", "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testSingletonLangLib1() {
        Symbol symbol = getSymbol(75, 4);
        TypeReferenceTypeSymbol typeRefTypeSymbol = (TypeReferenceTypeSymbol) symbol;
        UnionTypeSymbol unionSymbol = (UnionTypeSymbol) typeRefTypeSymbol.typeDescriptor();
        List<TypeSymbol> memberTypeDescriptors = unionSymbol.memberTypeDescriptors();
        TypeSymbol typeSymbol = memberTypeDescriptors.get(0);
        assertEquals(typeSymbol.typeKind(), SINGLETON);

        List<String> expFunctions = List.of("abs", "max", "min", "sum", "clone", "cloneReadOnly",
                "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson", "toJsonString",
                "fromJsonWithType", "mergeJson", "ensureType", "toHexString");

        assertLangLibList(typeSymbol.langLibMethods(), expFunctions);
    }

    @Test
    public void testSingletonLangLib2() {
        Symbol symbol = getSymbol(77, 4);
        TypeReferenceTypeSymbol typeRefTypeSymbol = (TypeReferenceTypeSymbol) symbol;
        UnionTypeSymbol unionSymbol = (UnionTypeSymbol) typeRefTypeSymbol.typeDescriptor();
        List<TypeSymbol> memberTypeDescriptors = unionSymbol.memberTypeDescriptors();
        TypeSymbol typeSymbol = memberTypeDescriptors.get(0);
        assertEquals(typeSymbol.typeKind(), SINGLETON);

        List<String> expFunctions = List.of("length", "iterator", "getCodePoint", "substring",
                "codePointCompare", "'join", "indexOf", "lastIndexOf", "startsWith", "endsWith", "toLowerAscii",
                "toUpperAscii", "equalsIgnoreCaseAscii", "trim", "toBytes",
                "toCodePointInts", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "fromBalString", "toJson", "toJsonString",
                "fromJsonWithType", "mergeJson", "ensureType", "fromJsonString",
                "fromJsonFloatString", "fromJsonDecimalString", "fromJsonStringWithType",
                "includes", "concat", "padStart", "padEnd", "padZero", "matches", "includesMatch");

        assertLangLibList(typeSymbol.langLibMethods(), expFunctions);
    }

    @Test
    public void testStringLangLib() {
        Symbol symbol = getSymbol(19, 11);
        TypeSymbol type = ((RecordFieldSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), STRING);

        List<String> expFunctions = List.of("length", "iterator", "getCodePoint", "substring",
                "codePointCompare", "'join", "indexOf", "lastIndexOf", "startsWith", "endsWith", "toLowerAscii",
                "toUpperAscii", "equalsIgnoreCaseAscii", "trim", "toBytes",
                "toCodePointInts", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "fromBalString", "toJson", "toJsonString",
                "fromJsonWithType", "mergeJson", "ensureType", "fromJsonString",
                "fromJsonFloatString", "fromJsonDecimalString", "fromJsonStringWithType",
                "includes", "concat", "padStart", "padEnd", "padZero", "matches",
                "includesMatch");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testFutureLangLib() {
        Symbol symbol = getSymbol(45, 16);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), FUTURE);

        List<String> expFunctions = List.of("cancel", "toString", "toBalString", "ensureType");
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testArrayLangLib() {
        Symbol symbol = getSymbol(47, 18);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), ARRAY);

        List<String> expFunctions = List.of("length", "iterator", "enumerate", "'map", "forEach", "filter",
                "reduce", "slice", "remove", "removeAll", "setLength", "reverse",
                "sort", "pop", "push", "shift", "unshift", "toString",
                "toBalString", "toStream", "ensureType", "some", "every");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testMapLangLib() {
        Symbol symbol = getSymbol(49, 16);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), MAP);

        List<String> expFunctions = List.of("length", "iterator", "get", "entries", "'map", "forEach",
                "filter", "reduce", "removeIfHasKey", "remove", "removeAll", "hasKey", "keys",
                "toArray", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType",
                "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(dataProvider = "XMLInfoProvider")
    public void testXMLLangLib(int line, int column, TypeDescKind expTypeKind, List<String> expFunctions) {
        Symbol symbol = getSymbol(line, column);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), expTypeKind);
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @DataProvider(name = "XMLInfoProvider")
    public Object[][] getXMLInfo() {
        List<String> expFunctions = List.of("length", "iterator", "forEach", "'map", "filter", "get",
                "slice", "strip", "elements", "children", "elementChildren", "clone", "cloneReadOnly",
                "cloneWithType", "isReadOnly", "toString", "toBalString", "toJson",
                "toJsonString", "ensureType", "text", "data");

        List<String> elementFuncs = List.of("getName", "setName", "getChildren", "setChildren", "getAttributes",
                "getDescendants");
        List<String> piFuncs = List.of("getTarget", "getContent");
        List<String> commentFuncs = List.of("getContent");

        return new Object[][]{
                {68, 8, XML, expFunctions},
                {69, 17, TYPE_REFERENCE, Stream.concat(expFunctions.stream(), elementFuncs.stream())
                        .collect(Collectors.toList())},
                {79, 31, TYPE_REFERENCE, Stream.concat(expFunctions.stream(), piFuncs.stream())
                        .collect(Collectors.toList())},
                {80, 17, TYPE_REFERENCE, Stream.concat(expFunctions.stream(), commentFuncs.stream())
                        .collect(Collectors.toList())},
                {81, 14, TYPE_REFERENCE, expFunctions},
        };
    }

    @Test
    public void testValueLangLib() {
        Symbol symbol = getSymbol(38, 9);
        FunctionTypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.returnTypeDescriptor().get().typeKind(), NIL);

        List<String> expFunctions = List.of("clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType", "mergeJson",
                "ensureType");

        assertLangLibList(type.returnTypeDescriptor().get().langLibMethods(), expFunctions);
    }

    @Test
    public void testObjectLangLib() {
        Symbol symbol = getSymbol(28, 6);
        ClassSymbol clazz = ((ClassSymbol) symbol);
        assertEquals(clazz.typeKind(), OBJECT);

        List<String> expFunctions = Collections.emptyList();
        assertLangLibList(clazz.langLibMethods(), expFunctions);
    }

    @Test
    public void testRecordLangLib() {
        Symbol symbol = getSymbol(18, 5);
        TypeSymbol type = ((TypeDefinitionSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), RECORD);

        List<String> expFunctions = List.of("length", "iterator", "get", "entries", "'map", "forEach",
                "filter", "reduce", "removeIfHasKey", "remove", "removeAll", "hasKey", "keys",
                "toArray", "clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "toJson", "toJsonString", "fromJsonWithType",
                "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(enabled = false)
    public void testTupleLangLib() {
        Symbol symbol = getSymbol(51, 28);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TUPLE);

        List<String> expFunctions = List.of("length", "iterator", "enumerate", "'map", "forEach", "filter",
                "reduce", "slice", "remove", "removeAll", "setLength", "indexOf", "lastIndexOf",
                "reverse", "sort", "pop", "push", "shift", "unshift", "toStream", "clone",
                "cloneReadOnly", "cloneWithType", "isReadOnly", "toString", "toBalString",
                "toJson", "toJsonString", "fromJsonWithType", "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testTypedescLangLib() {
        Symbol symbol = getSymbol(53, 22);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TYPEDESC);

        List<String> expFunctions =
                List.of("toString", "toBalString", "ensureType", "clone", "cloneReadOnly", "typeIds");
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testUnionType() {
        Symbol symbol = getSymbol(56, 21);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), UNION);

        List<String> expFunctions = List.of("clone", "cloneReadOnly", "cloneWithType", "isReadOnly",
                "toString", "toBalString", "toJson", "toJsonString",
                "fromJsonWithType", "mergeJson", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testErrorLangLib() {
        Symbol symbol = getSymbol(60, 10);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), ERROR);

        List<String> expFunctions = List.of("message", "cause", "detail", "stackTrace", "toString", "toBalString",
                "ensureType", "clone", "cloneReadOnly");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testStreamLangLib() {
        Symbol symbol = getSymbol(62, 22);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), STREAM);

        List<String> expFunctions = List.of("filter", "next", "'map", "reduce", "forEach", "iterator", "close",
                "toString", "toBalString", "ensureType");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test(dataProvider = "TableInfoProvider")
    public void testTableLangLib(int line, int column, List<String> expFunctions) {
        Symbol symbol = getSymbol(line, column);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TABLE);
        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @DataProvider(name = "TableInfoProvider")
    public Object[][] getTableInfo() {
        List<String> expFunctions = List.of("length", "put", "add", "removeAll", "toArray", "'map", "reduce",
                "forEach", "iterator", "toString", "toBalString", "clone", "cloneReadOnly",
                "cloneWithType", "isReadOnly", "toJson", "toJsonString", "ensureType");

        List<String> additionalFuncs = List.of("get", "remove", "removeIfHasKey", "filter", "hasKey", "keys");

        return new Object[][]{
                {64, 22, expFunctions},
                {65, 32, Stream.concat(expFunctions.stream(), additionalFuncs.stream()).collect(Collectors.toList())},
                {66, 37, expFunctions},
        };
    }

    @Test
    public void testIntersectionType() {
        Symbol symbol = getSymbol(71, 21);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INTERSECTION);

        List<String> expFunctions = List.of("reduce", "forEach", "shift", "length", "sort", "reverse",
                "toStream", "remove", "push", "filter", "pop", "lastIndexOf", "iterator", "removeAll",
                "setLength", "slice", "enumerate", "unshift", "'map", "indexOf",
                "cloneWithType", "cloneReadOnly", "toBalString", "toJson", "isReadOnly",
                "fromJsonWithType", "mergeJson", "clone", "ensureType", "toString",
                "toJsonString", "some", "every");

        assertLangLibList(type.langLibMethods(), expFunctions);
    }

    @Test
    public void testIntersectionOfReadonlyAndObjectType() {
        Symbol symbol = getSymbol(73, 41);
        assertEquals(symbol.kind(), SymbolKind.VARIABLE);
        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeSymbol.typeKind(), INTERSECTION);

        List<String> expFunctions = Collections.emptyList();
        assertLangLibList(typeSymbol.langLibMethods(), expFunctions);
    }

    @Test
    public void testAllErrorMemberUnionType() {
        Symbol symbol = getSymbol(83, 8);
        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeSymbol.typeKind(), TYPE_REFERENCE);

        List<String> expFunctions = List.of("message", "cause", "detail", "stackTrace", "toString", "toBalString",
                "ensureType", "clone", "cloneReadOnly");

        List<FunctionSymbol> langLibMethods = typeSymbol.langLibMethods();
        assertLangLibList(langLibMethods, expFunctions);
    }

    private Symbol getSymbol(int line, int column) {
        return model.symbol(srcFile, from(line, column)).get();
    }

    private void assertLangLibList(List<FunctionSymbol> langLib, List<String> expFunctions) {
        Set<String> langLibSet = langLib.stream().map(s -> s.getName().get()).collect(Collectors.toSet());

        for (String expFunction : expFunctions) {
            assertTrue(langLibSet.contains(expFunction), "Expected function '" + expFunction + "' not found");
        }

        assertEquals(langLibSet.size(), expFunctions.size(),
                "Found additional functions: " + getAdditionalFunctions(langLibSet, expFunctions).toString());
    }

    private Set<String> getAdditionalFunctions(Set<String> langLibSet, List<String> expFunctions) {
        Set<String> copy = new HashSet<>(langLibSet);
        copy.removeAll(expFunctions);
        return copy;
    }
}
