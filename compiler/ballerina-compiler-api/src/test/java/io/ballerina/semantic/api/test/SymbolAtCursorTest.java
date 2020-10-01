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

import io.ballerina.tools.text.LinePosition;
import org.ballerina.compiler.api.symbols.Symbol;
import org.ballerina.compiler.impl.BallerinaSemanticModel;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for the looking up a symbol of an identifier at a given position.
 *
 * @since 2.0.0
 */
public class SymbolAtCursorTest {

    private final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @Test(dataProvider = "BasicsPosProvider")
    public void testBasics(int line, int column, String expSymbolName) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_at_cursor_basic_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Optional<Symbol> symbol = model.symbol("symbol_at_cursor_basic_test.bal", LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.name(), expSymbolName));

        if (!symbol.isPresent()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "BasicsPosProvider")
    public Object[][] getPositionsForExactLookup() {
        return new Object[][]{
                {17, 7, null},
                {17, 8, "aString"},
                {17, 10, "aString"},
                {17, 15, null},
                {20, 9, null},
                {20, 10, "test"},
                {20, 12, "test"},
                {20, 14, null},
                {27, 12, null},
                {27, 13, "a"},
                {27, 14, null},
                {24, 8, "greet"},
                {41, 8, "Person"},
                {42, 8, "PersonObj"},
                {43, 21, "pObj"},
                {43, 30, "PersonObj.getFullName"},
                {44, 12, "p"},
                {44, 17, "name"},
                {45, 20, "p"},
                {45, 23, "name"},
                {50, 5, null},
                {50, 9, "Person"},
                {50, 15, null},
                {51, 15, "name"},
                {54, 11, "PersonObj"},
                {55, 15, "fname"},
                {56, 15, "lname"},
                {59, 16, "fname"},
                {60, 12, "self"},
                {64, 24, "fname"},
                {64, 42, "lname"},
        };
    }

    @Test(dataProvider = "EnumPosProvider")
    public void testEnum(int line, int column, String expSymbolName) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_at_cursor_enum_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Optional<Symbol> symbol = model.symbol("symbol_at_cursor_enum_test.bal", LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.name(), expSymbolName));

        if (!symbol.isPresent()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "EnumPosProvider")
    public Object[][] getEnumPos() {
        return new Object[][]{
                {18, 7, "RED"},
//                {22, 19, "RED"}, // TODO: issue #25841
                {23, 9, "Colour"},
                {27, 46, "Colour"},
//                {31, 18, "GREEN"}, // TODO: issue #25841
//                {32, 29, "BLUE"}, // TODO: issue #25841
        };
    }

    @Test(dataProvider = "WorkerSymbolPosProvider")
    public void testWorkers(int line, int column, String expSymbolName) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_workers_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Optional<Symbol> symbol = model.symbol("symbol_lookup_with_workers_test.bal", LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.name(), expSymbolName));

        if (!symbol.isPresent()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "WorkerSymbolPosProvider")
    public Object[][] getWorkerPos() {
        return new Object[][]{
                {22, 13, "w1"},
                {24, 13, "w2"},
                {27, 14, "w2"},
                {29, 24, "w2"},
                {35, 15, "w1"},
                {37, 21, "w1"},
                {40, 21, "w2"},
        };
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }
}
