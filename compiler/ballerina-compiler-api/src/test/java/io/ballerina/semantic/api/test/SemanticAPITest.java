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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test cases for the semantic API.
 *
 * @since 2.0.0
 */
public class SemanticAPITest {

    private CompilerContext context;
    private CompileResult result;

    @BeforeClass
    public void setup() {
        Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();
        Path sourcePath = Paths.get("test-src/test.bal");
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());

        context = new CompilerContext();
        result = BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }

    @Test
    public void test() {
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg.compUnits.get(0), pkg, context);
        List<Symbol> symbols = model.visibleSymbols(LinePosition.from(3, 14));
    }
}
