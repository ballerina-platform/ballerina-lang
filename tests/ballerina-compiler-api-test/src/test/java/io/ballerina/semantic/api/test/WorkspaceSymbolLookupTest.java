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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

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

    @Test
    public void testWSSymbolLookup() {
        Project project = BCompileUtil.loadProject("test-src/testproject/");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();

        List<Symbol> symbols = model.moduleSymbols();

        List<String> allSymbols = getSymbolNames(pkg.symbol, 0, SOURCE);
        assertList(symbols, allSymbols);
    }
}
