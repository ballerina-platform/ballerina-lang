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

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getModule;

/**
 * Test cases for the finding all references of a symbol across a module, in tests.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInTestsTest extends FindAllReferencesTest {

    @Override
    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject(getTestSourcePath());
        Module baz = getModule(project, "baz");
        model = project.currentPackage().getCompilation().getSemanticModel(baz.moduleId());
        DocumentId id = baz.testDocumentIds().iterator().next();
        srcFile = baz.document(id);
    }

    @Override
    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {25, 14, location(16, 16, 19, "functions.bal"),
                        List.of(location(16, 16, 19, "functions.bal"),
                                location(25, 14, 17, getFileName()))
                },
                {20, 22, location(16, 13, 15, "constants.bal"),
                        List.of(location(16, 13, 15, "constants.bal"),
                                location(20, 22, 24, getFileName()))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/test-project";
    }
}
