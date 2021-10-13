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

package io.ballerina.semantic.api.test.visiblesymbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolsInFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Base test class for visible symbols tests.
 */
public abstract class BaseVisibleSymbolsTest {

    private SemanticModel model;
    private ModuleID moduleID;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject(getTestSourcePath());
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        model = packageCompilation.getSemanticModel(defaultModuleId);
        srcFile = getDocumentForSingleSource(project);
        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        moduleID = new BallerinaModuleID(pkg.packageID);
    }

    @Test(dataProvider = "PositionProvider")
    public void testVisibleSymbols(int line, int column, List<ExpectedSymbolInfo> expSymbols) {
        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, column, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols.size());

        for (ExpectedSymbolInfo symInfo : expSymbols) {
            assertTrue(symbolsInFile.containsKey(symInfo.expName), "Symbol not found: " + symInfo);
            assertEquals(symbolsInFile.get(symInfo.expName).kind(), symInfo.expKind);
        }

        getAdditionalAssertions().accept(symbolsInFile);
    }

    @DataProvider(name = "PositionProvider")
    public abstract Object[][] getLookupPositions();

    abstract String getTestSourcePath();

    Consumer<Map<String, Symbol>> getAdditionalAssertions() {
        return (symbolsInFile) -> {
        };
    }

    List<ExpectedSymbolInfo> concat(List<ExpectedSymbolInfo> list, ExpectedSymbolInfo... symbols) {
        return Stream.concat(list.stream(), Stream.of(symbols)).collect(Collectors.toList());
    }

    static class ExpectedSymbolInfo {
        String expName;
        SymbolKind expKind;

        private ExpectedSymbolInfo(String expName, SymbolKind expKind) {
            this.expName = expName;
            this.expKind = expKind;
        }

        static ExpectedSymbolInfo from(String expName, SymbolKind expKind) {
            return new ExpectedSymbolInfo(expName, expKind);
        }

        @Override
        public String toString() {
            return String.format("[%s : %s]", this.expName, this.expKind);
        }
    }
}
