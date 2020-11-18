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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;

/**
 * Test cases for the API for getting the workspace symbols. i.e., all module-level symbols.
 *
 * @since 2.0.0
 */
public class WorkspaceSymbolLookupTest {

    private final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @Test
    public void testWSSymbolLookup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/test-project/", "foo", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        List<Symbol> symbols = model.moduleLevelSymbols();

        List<String> allSymbols = getSymbolNames(pkg.symbol, 0, SOURCE);
        assertList(symbols, allSymbols);
    }

    private CompileResult compile(String sourceRoot, String module, CompilerContext context) {
        String sourceRootAbsolute = BCompileUtil.concatFileName(sourceRoot, resourceDir.toAbsolutePath());
        return BCompileUtil.compileOnJBallerina(context, sourceRootAbsolute, module, false, true, false);
    }
}
