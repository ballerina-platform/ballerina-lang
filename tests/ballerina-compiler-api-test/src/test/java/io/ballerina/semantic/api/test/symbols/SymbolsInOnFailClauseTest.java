/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for on-fail clause.
 */
public class SymbolsInOnFailClauseTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_on_fail_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "OnFailPosProvider")
    public void testOnFailSymbolPositions(int line, int sCol, int eCol, String name) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from(srcFile.name(), LinePosition.from(line, sCol), LinePosition.from(line, eCol)));
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), TypeDescKind.ERROR);
        type.get().getName().ifPresent(tName -> assertEquals(tName, name));
    }

    @DataProvider(name = "OnFailPosProvider")
    public Object[][] getOnFailSymbolPositions() {
        return new Object[][]{
                {21, 20, 23, "err"},
                {30, 20, 23, "err"},
                {38, 20, 23, "err"},
                {54, 20, 21, "e"},
                {67, 23, 26, "err"},
                {79, 19, 20, "e"},
                {88, 19, 20, "e"}
        };
    }
}
