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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.compiler.CompilerPhase.COMPILER_PLUGIN;
import static org.ballerinalang.test.util.BCompileUtil.compile;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for the find all references API.
 *
 * @since 2.0.0
 */
public class FindAllReferencesTest {

    private static final List<Location> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());
    private static final String FILE_NAME = "find_all_ref_test.bal";
    private SemanticModel model;

    @BeforeClass
    public void setup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/find_all_ref_test.bal", context, COMPILER_PLUGIN);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
    }

    @Test(dataProvider = "Provider1")
    public void testFindAllRef(int line, int col, List<Location> expLocations) {
        List<Location> locations = model.allReferences(FILE_NAME, LinePosition.from(line, col));
        assertLocations(locations, expLocations);
    }

    @DataProvider(name = "Provider1")
    public Object[][] getPos1() {
        return new Object[][]{
                {22, 12, List.of(location(22, 12, 13),
                                 location(23, 21, 22),
                                 location(29, 37, 38),
                                 location(32, 25, 26))
                },
                {23, 25, List.of(location(19, 8, 9),
                                 location(23, 25, 26),
                                 location(29, 29, 30),
                                 location(35, 21, 22))
                },
                {16, 4, List.of(location(16, 4, 5),
                                location(19, 17, 18)),
                },
                {29, 20, List.of(location(29, 20, 21))},
                {29, 19, EMPTY_LIST}
        };
    }

    private void assertLocations(List<Location> locations, List<Location> expLocations) {
        assertEquals(locations.size(), expLocations.size());

        for (int i = 0; i < expLocations.size(); i++) {
            Location expLocation = expLocations.get(i);
            Location location = locations.get(i);

            assertEquals(location.lineRange(), expLocation.lineRange());
        }
    }

    private Location location(int line, int startCol, int endCol) {
        return new BLangDiagnosticLocation(FILE_NAME, line, line, startCol, endCol);
    }
}
