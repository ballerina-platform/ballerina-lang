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

package io.ballerina.semantic.api.test.util;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Utility functions to support test cases written for the semantic API.
 *
 * @since 2.0.0
 */
public class SemanticAPITestUtils {

    private static final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    public static CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }

    public static void assertList(List<? extends Symbol> actualValues, List<String> expectedValues) {
        Map<String, Symbol> symbols = actualValues.stream().collect(Collectors.toMap(Symbol::name, s -> s));
        assertList(symbols, expectedValues);
    }

    public static void assertList(Map<String, Symbol> actualValues, List<String> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (String val : expectedValues) {
            assertTrue(actualValues.containsKey(val), "Symbol not found: " + val);
        }
    }

    public static Map<String, Symbol> getSymbolsInFile(BallerinaSemanticModel model, String srcFile, int line,
                                                       int column, ModuleID moduleID) {
        List<Symbol> allInScopeSymbols = model.visibleSymbols(srcFile, LinePosition.from(line, column));
        return allInScopeSymbols.stream()
                .filter(s -> s.moduleID().equals(moduleID))
                .collect(Collectors.toMap(Symbol::name, s -> s));
    }

    public static List<String> getSymbolNames(List<String> mainList, String... args) {
        return Stream.concat(mainList.stream(), Stream.of(args)).collect(Collectors.toList());
    }

    public static List<String> getSymbolNames(List<String>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<String> getSymbolNames(BPackageSymbol pkgSymbol, int symTag) {
        List<String> symbolNames = new ArrayList<>();
        for (Map.Entry<Name, Scope.ScopeEntry> entry : pkgSymbol.scope.entries.entrySet()) {
            Name name = entry.getKey();
            Scope.ScopeEntry value = entry.getValue();

            if (value.symbol != null && (value.symbol.tag & symTag) == symTag
                    && Symbols.isFlagOn(value.symbol.flags, Flags.PUBLIC) && value.symbol.origin == COMPILED_SOURCE) {
                symbolNames.add(name.value);
            }
        }
        return symbolNames;
    }

    public static List<String> getSymbolNames(BPackageSymbol pkgSymbol, int symTag, SymbolOrigin origin) {
        List<String> symbolNames = new ArrayList<>();
        for (Map.Entry<Name, Scope.ScopeEntry> entry : pkgSymbol.scope.entries.entrySet()) {
            Name name = entry.getKey();
            Scope.ScopeEntry value = entry.getValue();

            if (value.symbol != null && (value.symbol.tag & symTag) == symTag && value.symbol.origin == origin) {
                symbolNames.add(name.value);
            }
        }
        return symbolNames;
    }
}
