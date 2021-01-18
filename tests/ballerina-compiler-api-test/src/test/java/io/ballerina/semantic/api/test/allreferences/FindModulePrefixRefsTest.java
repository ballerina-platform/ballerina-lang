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

import org.ballerinalang.test.balo.BaloCreator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for the find all references API when the symbol is a module prefix.
 *
 * @since 2.0.0
 */
@Test
public class FindModulePrefixRefsTest extends FindAllReferencesTest {

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/test-project", "testorg", "baz");
        super.setup();
    }

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 15, List.of(location(16, 15, 18),
                                 location(19, 0, 3),
                                 location(22, 4, 7),
                                 location(36, 27, 30),
                                 location(39, 28, 31),
                                 location(40, 18, 21))
                },
                {25, 16, List.of(location(25, 16, 20),
                                 location(31, 24, 28)),
                },
                {35, 27, List.of(location(17, 33, 36),
                                 location(35, 27, 30)),
                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_module_prefix.bal";
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/test-project", "testorg", "baz");
    }
}
