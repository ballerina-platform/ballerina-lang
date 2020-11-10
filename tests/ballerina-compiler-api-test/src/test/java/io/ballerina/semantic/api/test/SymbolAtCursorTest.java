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

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.text.LinePosition;
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
                {16, 6, null},
                {16, 7, "aString"},
                {16, 9, "aString"},
                {16, 14, null},
                {19, 8, null},
                {19, 9, "test"},
                {19, 11, "test"},
                {19, 13, null},
                {26, 11, null},
                {26, 12, "a"},
                {26, 13, null},
                {23, 7, "greet"},
                {40, 7, "Person"},
                {41, 7, "PersonObj"},
                {42, 20, "pObj"},
                {42, 29, "PersonObj.getFullName"},
                {43, 11, "p"},
                {43, 15, "name"},
                {44, 19, "p"},
                {44, 22, "name"},
                {49, 4, null},
                {49, 8, "Person"},
                {49, 14, null},
                {50, 14, "name"},
                {53, 10, "PersonObj"},
                {54, 14, "fname"},
                {55, 14, "lname"},
                {58, 15, "fname"},
                {59, 11, "self"},
                {63, 23, "fname"},
                {63, 41, "lname"},
                {72, 4, "test"},
                {72, 7, "test"},
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
                {17, 6, "RED"},
//                {21, 18, "RED"}, // TODO: issue #25841
                {22, 8, "Colour"},
                {26, 45, "Colour"},
//                {30, 17, "GREEN"}, // TODO: issue #25841
//                {31, 28, "BLUE"}, // TODO: issue #25841
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
                {21, 12, "w1"},
                {23, 12, "w2"},
                {26, 13, "w2"},
                {28, 23, "w2"},
                {34, 14, "w1"},
                {36, 20, "w1"},
                {39, 20, "w2"},
        };
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }
}
