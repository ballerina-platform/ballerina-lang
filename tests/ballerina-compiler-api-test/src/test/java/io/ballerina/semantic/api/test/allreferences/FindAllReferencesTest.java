/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test.allreferences;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the find all references API.
 *
 * @since 2.0.0
 */
public abstract class FindAllReferencesTest {

    public static final List<Location> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());
    protected SemanticModel model;
    protected Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject(getTestSourcePath());
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFindAllReferencesUsingLocation(int line, int col, Location def, List<Location> expLocations) {
        List<Location> locations = model.references(srcFile, LinePosition.from(line, col));
        assertLocations(locations, expLocations);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFindAllReferencesUsingSymbol(int line, int col, Location def, List<Location> expLocations) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expLocations.isEmpty()) {
            assertTrue(symbol.isEmpty());
            return;
        }

        List<Location> locations = model.references(symbol.get());
        assertLocations(locations, expLocations);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFindAllReferencesUsingLocationSansDef(int line, int col, Location def,
                                                          List<Location> expLocations) {
        List<Location> locations = model.references(srcFile, LinePosition.from(line, col), false);
        List<Location> expLocationsSansDef = new ArrayList<>(expLocations);
        expLocationsSansDef.remove(def);
        assertLocations(locations, expLocationsSansDef);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFindAllReferencesUsingSymbolSansDef(int line, int col, Location def, List<Location> expLocations) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expLocations.isEmpty()) {
            assertTrue(symbol.isEmpty());
            return;
        }

        List<Location> locations = model.references(symbol.get(), false);
        List<Location> expLocationsSansDef = new ArrayList<>(expLocations);
        expLocationsSansDef.remove(def);
        assertLocations(locations, expLocationsSansDef);
    }

    @DataProvider(name = "PositionProvider")
    public abstract Object[][] getLookupPositions();

    public abstract String getTestSourcePath();

    public String getFileName() {
        return this.srcFile.name();
    }

    // Util methods

    protected Location location(int line, int startCol, int endCol) {
        return new BLangDiagnosticLocation(getFileName(), line, line, startCol, endCol);
    }

    protected static Location location(int line, int startCol, int endCol, String fileName) {
        return new BLangDiagnosticLocation(fileName, line, line, startCol, endCol);
    }

    protected static void assertLocations(List<Location> locations, List<Location> expLocations) {
        assertEquals(locations.size(), expLocations.size());

        List<LineRange> lineRanges = new ArrayList<>();
        for (Location location : locations) {
            lineRanges.add(location.lineRange());
        }

        for (Location expLocation : expLocations) {
            assertTrue(lineRanges.contains(expLocation.lineRange()));
        }
    }
}
