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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.incompletesource;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of undefined types as field types.
 *
 * @since 2.0.0
 */
public class InvalidFieldTypeTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/incomplete-sources/undefined_type_in_fields.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "CursorPosProvider1")
    public void testSymbolAtCursor(int line, int col) {
        assertTrue(model.symbol(srcFile, from(line, col)).isEmpty());
    }

    @DataProvider(name = "CursorPosProvider1")
    public Object[][] getCursorPos1() {
        return new Object[][]{
                {17, 16},
                {22, 13},
                {27, 13},
        };
    }

    @Test(dataProvider = "CursorPosProvider2")
    public void testVisibleSymbols(int line, int col, String name) {
        Map<String, Symbol> symbolsInFile =
                SemanticAPITestUtils.getSymbolsInFile(model, srcFile, line, col,
                                                      new BallerinaModuleID(PackageID.DEFAULT));
        assertFalse(symbolsInFile.containsKey(name));
    }

    @DataProvider(name = "CursorPosProvider2")
    public Object[][] getCursorPos2() {
        return new Object[][]{
                {18, 5, "c"},
                {23, 5, "name"},
                {28, 5, "name"},
        };
    }
}
